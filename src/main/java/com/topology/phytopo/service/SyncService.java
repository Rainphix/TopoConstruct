package com.topology.phytopo.service;

import cn.hutool.json.JSONUtil;
import com.topology.phytopo.entity.TopoNe;
import com.topology.phytopo.entity.TopoSubnet;
import com.topology.phytopo.kafka.EamChangeMessage;
import com.topology.phytopo.mapper.TopoMergeGroupMapper;
import com.topology.phytopo.mapper.TopoNeMapper;
import com.topology.phytopo.mapper.TopoSubnetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据同步服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {

    @Value("${topology.sync.batch-size:500}")
    private int batchSize;

    @Value("${topology.sync.retry-times:3}")
    private int retryTimes;

    @Value("${topology.sync.retry-interval:1000}")
    private long retryInterval;

    private final TopoSubnetMapper subnetMapper;
    private final TopoNeMapper neMapper;
    private final TopoMergeGroupMapper mergeGroupMapper;
    private final MergeService mergeService;

    /**
     * 处理EAM变更消息
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleChange(EamChangeMessage message) {
        log.info("处理EAM变更: type={}, dn={}",
                message.getChangeType(), message.getDn());

        switch (message.getChangeType()) {
            case "CREATE" -> handleCreate(message);
            case "UPDATE" -> handleUpdate(message);
            case "DELETE" -> handleDelete(message);
            default -> log.warn("未知的变更类型: {}", message.getChangeType());
        }
    }

    /**
     * 处理创建消息
     */
    private void handleCreate(EamChangeMessage message) {
        String objectType = message.getObjectType();
        if ("SUBNET".equals(objectType)) {
            TopoSubnet subnet = convertToSubnet(message);
            subnetMapper.insert(subnet);
            log.info("创建子网成功: dn={}", subnet.getDn());

            // 检查是否需要合并
            mergeService.checkAndMerge(subnet.getParentDn());
        } else {
            TopoNe ne = convertToNe(message);
            neMapper.insert(ne);
            log.info("创建网元成功: dn={}", ne.getDn());
        }
    }

    /**
     * 处理更新消息
     */
    private void handleUpdate(EamChangeMessage message) {
        String objectType = message.getObjectType();
        if ("SUBNET".equals(objectType)) {
            TopoSubnet existing = subnetMapper.selectByDn(message.getDn());
            if (existing != null) {
                TopoSubnet subnet = convertToSubnet(message);
                subnetMapper.update(subnet);
                log.info("更新子网成功: dn={}", subnet.getDn());
            }
        } else {
            TopoNe existing = neMapper.selectByDn(message.getDn());
            if (existing != null) {
                TopoNe ne = convertToNe(message);
                neMapper.update(ne);
                log.info("更新网元成功: dn={}", ne.getDn());
            }
        }
    }

    /**
     * 处理删除消息
     */
    private void handleDelete(EamChangeMessage message) {
        String objectType = message.getObjectType();
        if ("SUBNET".equals(objectType)) {
            subnetMapper.deleteByDn(message.getDn());
            log.info("删除子网成功: dn={}", message.getDn());
        } else {
            neMapper.deleteByDn(message.getDn());
            log.info("删除网元成功: dn={}", message.getDn());
        }
    }

    /**
     * 全量同步
     */
    @Transactional(rollbackFor = Exception.class)
    public void fullSync() {
        log.info("开始全量同步...");
        long startTime = System.currentTimeMillis();

        // TODO: 调用EAM接口查询Default分组下的所有数据
        // 这里需要实现实际的数据同步逻辑

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("全量同步完成, 耗时 {} ms", elapsed);
    }

    /**
     * 计算层级
     */
    private int calculateLayer(String parentDn) {
        if (parentDn == null || "DN=Root".equals(parentDn)) {
            return 0;
        }
        TopoSubnet parent = subnetMapper.selectByDn(parentDn);
        if (parent == null) {
            return 0;
        }
        return parent.getLayer() + 1;
    }

    private TopoSubnet convertToSubnet(EamChangeMessage message) {
        TopoSubnet subnet = new TopoSubnet();
        subnet.setDn(message.getDn());
        subnet.setName(message.getName());
        subnet.setDisplayName(message.getDisplayName());
        subnet.setParentDn(message.getParentDn());
        subnet.setParentType("SUBNET");
        subnet.setLayer(calculateLayer(message.getParentDn()));
        subnet.setAddress(message.getAddress());
        subnet.setLocation(message.getLocation());
        subnet.setMaintainer(message.getMaintainer());
        subnet.setContact(message.getContact());
        subnet.setAlarmStatus(0);
        subnet.setCreatedTime(System.currentTimeMillis());
        subnet.setUpdatedTime(System.currentTimeMillis());
        subnet.setSyncTime(System.currentTimeMillis());
        return subnet;
    }

    private TopoNe convertToNe(EamChangeMessage message) {
        TopoNe ne = new TopoNe();
        ne.setDn(message.getDn());
        ne.setName(message.getName());
        ne.setDisplayName(message.getDisplayName());
        ne.setParentDn(message.getParentDn());
        ne.setParentType("SUBNET");
        ne.setNeType(detectNeType(message.getName()));
        ne.setAddress(message.getAddress());
        ne.setLocation(message.getLocation());
        ne.setMaintainer(message.getMaintainer());
        ne.setContact(message.getContact());
        ne.setAlarmStatus(0);
        ne.setStatus(1);
        ne.setCreatedTime(System.currentTimeMillis());
        ne.setUpdatedTime(System.currentTimeMillis());
        ne.setSyncTime(System.currentTimeMillis());
        return ne;
    }

    /**
     * 识别网元类型
     */
    private String detectNeType(String name) {
        if (name == null) {
            return "DEFAULT";
        }
        String nameLower = name.toLowerCase();
        if (nameLower.contains("firewall")) {
            return "FIREWALL";
        } else if (nameLower.contains("switch")) {
            return "SWITCH";
        } else if (nameLower.contains("server")) {
            return "SERVER";
        } else if (nameLower.contains("storage")) {
            return "STORAGE";
        } else if (nameLower.contains("gateway")) {
            return "GATEWAY";
        } else if (nameLower.contains("chassis")) {
            return "CHASSIS";
        } else if (nameLower.contains("rack")) {
            return "RACK";
        }
        return "DEFAULT";
    }
}
