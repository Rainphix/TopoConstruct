package com.topology.phytopo.service;

import com.topology.phytopo.common.enums.MergeModeEnum;
import com.topology.phytopo.dto.request.MergeConfigRequest;
import com.topology.phytopo.entity.TopoMergeGroup;
import com.topology.phytopo.entity.TopoNe;
import com.topology.phytopo.entity.TopoSubnet;
import com.topology.phytopo.mapper.TopoMergeGroupMapper;
import com.topology.phytopo.mapper.TopoNeMapper;
import com.topology.phytopo.mapper.TopoPositionMapper;
import com.topology.phytopo.mapper.TopoSubnetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 同类型合并服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MergeService {

    @Value("${topology.merge.enabled:false}")
    private boolean mergeEnabled;

    @Value("${topology.merge.threshold:20}")
    private int mergeThreshold;

    @Value("${topology.merge.mode:THRESHOLD}")
    private String mergeMode;

    private final TopoSubnetMapper subnetMapper;
    private final TopoNeMapper neMapper;
    private final TopoMergeGroupMapper mergeGroupMapper;
    private final TopoPositionMapper positionMapper;

    /**
     * 检查并执行合并
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkAndMerge(String parentDn) {
        if (!mergeEnabled) {
            log.debug("合并功能未开启");
            return;
        }

        // 获取该父节点下的所有网元
        List<TopoNe> neList = neMapper.selectByParentDn(parentDn);

        // 按类型分组
        Map<String, List<TopoNe>> typeGroups = neList.stream()
                .collect(Collectors.groupingBy(TopoNe::getNeType));

        // 处理每种类型
        for (Map.Entry<String, List<TopoNe>> entry : typeGroups.entrySet()) {
            String neType = entry.getKey();
            List<TopoNe> typeNeList = entry.getValue();
            processTypeMerge(parentDn, neType, typeNeList);
        }
    }

    /**
     * 处理单个类型的合并
     */
    private void processTypeMerge(String parentDn, String neType, List<TopoNe> neList) {
        MergeModeEnum mode = MergeModeEnum.fromCode(mergeMode);

        boolean shouldMerge = false;
        if (mode == MergeModeEnum.THRESHOLD) {
            shouldMerge = neList.size() >= mergeThreshold;
        } else if (mode == MergeModeEnum.ALL) {
            shouldMerge = neList.size() > 1;
        }

        if (!shouldMerge) {
            log.debug("类型 {} 数量 {} 不满足合并条件", neType, neList.size());
            return;
        }

        // 检查是否已有合并组
        List<TopoMergeGroup> existingGroups = mergeGroupMapper.selectByParentAndNeType(parentDn, neType);
        if (!existingGroups.isEmpty()) {
            log.debug("类型 {} 已存在合并组，跳过", neType);
            return;
        }

        // 计算需要的组数
        int totalNe = neList.size();
        int groupCount = (int) Math.ceil((double) totalNe / mergeThreshold);

        log.info("类型 {} 需要创建 {} 个合并组", neType, groupCount);

        // 创建合并组
        for (int i = 0; i < groupCount; i++) {
            int fromIndex = i * mergeThreshold;
            int toIndex = Math.min((i + 1) * mergeThreshold, totalNe);
            List<TopoNe> groupNeList = neList.subList(fromIndex, toIndex);

            createMergeGroup(parentDn, neType, i + 1, groupNeList);
        }
    }

    /**
     * 创建单个合并组
     */
    private void createMergeGroup(String parentDn, String neType, int groupIndex, List<TopoNe> neList) {
        long now = System.currentTimeMillis();

        // 1. 创建合并组子网
        String subnetDn = generateMergeSubnetDn(parentDn, neType, groupIndex);
        String subnetName = neType + groupIndex;

        // 检查是否已存在
        if (subnetMapper.existsByDn(subnetDn)) {
            log.debug("合并组子网已存在: {}", subnetDn);
            return;
        }

        TopoSubnet subnet = new TopoSubnet();
        subnet.setDn(subnetDn);
        subnet.setName(subnetName);
        subnet.setDisplayName(subnetName);
        subnet.setParentDn(parentDn);
        subnet.setParentType("SUBNET");
        subnet.setIsMergeGroup(true);
        subnet.setMergeType(neType);
        subnet.setCreatedTime(now);
        subnet.setUpdatedTime(now);

        subnetMapper.insert(subnet);
        log.info("创建合并组子网: dn={}, name={}", subnetDn, subnetName);

        // 2. 记录合并组信息
        TopoMergeGroup mergeGroup = new TopoMergeGroup();
        mergeGroup.setSubnetDn(subnetDn);
        mergeGroup.setParentDn(parentDn);
        mergeGroup.setNeType(neType);
        mergeGroup.setGroupIndex(groupIndex);
        mergeGroup.setMemberCount(neList.size());
        mergeGroup.setCreatedTime(now);
        mergeGroup.setUpdatedTime(now);
        mergeGroupMapper.insert(mergeGroup);

        // 3. 更新网元的父节点
        List<String> neDnList = neList.stream()
                .map(TopoNe::getDn)
                .collect(Collectors.toList());
        neMapper.batchUpdateParentDn(neDnList, subnetDn, System.currentTimeMillis());
        log.info("更新 {} 个网元父节点到: {}", neList.size(), subnetDn);
    }

    /**
     * 生成合并组子网DN
     */
    private String generateMergeSubnetDn(String parentDn, String neType, int groupIndex) {
        return "DN=MERGE_" + parentDn.replace("DN=", "") + "_" + neType + "_" + groupIndex;
    }

    /**
     * 取消所有合并
     */
    @Transactional(rollbackFor = Exception.class)
    public void disableMerge() {
        log.info("开始取消合并...");

        // 查询所有合并组
        List<TopoMergeGroup> mergeGroups = mergeGroupMapper.selectAll();
        if (mergeGroups.isEmpty()) {
            log.info("没有需要取消的合并组");
            return;
        }

        for (TopoMergeGroup group : mergeGroups) {
            // 将合并组下的网元移回原父节点
            List<TopoNe> neList = neMapper.selectByParentDn(group.getSubnetDn());
            if (!neList.isEmpty()) {
                List<String> neDnList = neList.stream()
                        .map(TopoNe::getDn)
                        .collect(Collectors.toList());
                neMapper.batchUpdateParentDn(neDnList, group.getParentDn(), System.currentTimeMillis());
                log.info("将 {} 个网元移回父节点: {}", neList.size(), group.getParentDn());
            }

            // 删除坐标记录
            positionMapper.deleteBySubnetDn(group.getSubnetDn());

            // 删除合并组记录
            mergeGroupMapper.deleteBySubnetDn(group.getSubnetDn());

            // 删除合并组子网
            subnetMapper.deleteByDn(group.getSubnetDn());
            log.info("删除合并组: {}", group.getSubnetDn());
        }

        log.info("取消合并完成，共处理 {} 个合并组", mergeGroups.size());
    }

    /**
     * 开启合并（对现有数据执行合并）
     */
    @Transactional(rollbackFor = Exception.class)
    public void enableMerge() {
        log.info("开始开启合并...");

        // 获取所有顶层子网
        List<TopoSubnet> topSubnets = subnetMapper.selectByParentDn(null);
        for (TopoSubnet subnet : topSubnets) {
            processSubnetRecursive(subnet.getDn());
        }

        log.info("开启合并完成");
    }

    /**
     * 递归处理子网
     */
    private void processSubnetRecursive(String subnetDn) {
        // 检查当前子网下的合并
        checkAndMerge(subnetDn);

        // 递归处理子子网（跳过合并组子网，避免对已合并的组再次合并）
        List<TopoSubnet> childSubnets = subnetMapper.selectByParentDn(subnetDn);
        for (TopoSubnet child : childSubnets) {
            if (!Boolean.TRUE.equals(child.getIsMergeGroup())) {
                processSubnetRecursive(child.getDn());
            }
        }
    }

    /**
     * 更新合并配置
     */
    public void updateConfig(MergeConfigRequest request) {
        boolean wasEnabled = this.mergeEnabled;

        if (request.getEnabled() != null) {
            this.mergeEnabled = request.getEnabled();
        }
        if (request.getThreshold() != null) {
            this.mergeThreshold = request.getThreshold();
        }
        if (request.getMode() != null) {
            this.mergeMode = request.getMode();
        }

        log.info("合并配置已更新: enabled={}, threshold={}, mode={}",
                mergeEnabled, mergeThreshold, mergeMode);

        // 如果从开启变为关闭，执行取消合并
        if (wasEnabled && !this.mergeEnabled) {
            disableMerge();
        }
        // 如果从关闭变为开启，执行合并
        else if (!wasEnabled && this.mergeEnabled) {
            enableMerge();
        }
    }

    /**
     * 获取当前合并配置
     */
    public MergeConfigRequest getConfig() {
        MergeConfigRequest config = new MergeConfigRequest();
        config.setEnabled(this.mergeEnabled);
        config.setThreshold(this.mergeThreshold);
        config.setMode(this.mergeMode);
        return config;
    }
}
