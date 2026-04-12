package com.topology.phytopo.service;

import com.topology.phytopo.client.EamClient;
import com.topology.phytopo.entity.TopoNe;
import com.topology.phytopo.entity.TopoSubnet;
import com.topology.phytopo.mapper.TopoNeMapper;
import com.topology.phytopo.mapper.TopoSubnetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 初始化服务 - 从EAM全量加载数据
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InitService {

    @Value("${topology.init.enabled:false}")
    private Boolean initEnabled;

    @Value("${topology.init.async:true}")
    private Boolean initAsync;

    @Value("${eam.default-group:Default}")
    private String defaultGroup;

    private final EamClient eamClient;
    private final TopoSubnetMapper subnetMapper;
    private final TopoNeMapper neMapper;
    private final MergeService mergeService;

    // 待查询队列
    private final ConcurrentLinkedQueue<Map<String, Object>> pendingQueue = new ConcurrentLinkedQueue<>();
    // 待插入队列
    private final ConcurrentLinkedQueue<Object> insertQueue = new ConcurrentLinkedQueue<>();

    /**
     * 应用启动时执行初始化
     */
    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        if (!initEnabled) {
            log.info("初始化功能未开启");
            return;
        }

        if (initAsync) {
            CompletableFuture.runAsync(this::doInit);
        } else {
            doInit();
        }
    }

    /**
     * 执行初始化
     */
    private void doInit() {
        log.info("开始初始化同步...");
        long startTime = System.currentTimeMillis();

        try {
            // 1. 查询Default分组下的所有子网和设备
           List<Map<String, Object>> defaultData = eamClient.queryDefaultGroup(defaultGroup);
            log.info("从EAM查询到 {} 条数据", defaultData.size());

            // 2. 分类并加入待查询队列
            for (Map<String, Object> data : defaultData) {
                pendingQueue.offer(data);
            }

            // 3. 开始处理队列
            processPendingQueue();

            // 4. 批量入库
            batchInsert();

            long endTime = System.currentTimeMillis();
            log.info("初始化同步完成, 耗时: {} ms", endTime - startTime);

        } catch (Exception e) {
            log.error("初始化同步失败", e);
        }
    }

    /**
     * 处理待查询队列
     */
    private void processPendingQueue() {
        while (!pendingQueue.isEmpty()) {
            Map<String, Object> data = pendingQueue.poll();
            if (data == null) {
                continue;
            }

            String objectType = (String) data.get("objectType");
            if ("SUBNET".equals(objectType)) {
                // 如果是子网，查询子网下的子元素
                String dn = (String) data.get("dn");
                List<Map<String, Object>> children = eamClient.queryChildren(dn);
                if (children != null && !children.isEmpty()) {
                    for (Map<String, Object> child : children) {
                        pendingQueue.offer(child);
                    }
                }
            }

            // 加入待插入队列
            insertQueue.offer(data);
        }
    }

    /**
     * 批量入库
     */
    private void batchInsert() {
        List<TopoSubnet> subnets = new ArrayList<>();
        List<TopoNe> nes = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (Object data : insertQueue) {
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data;
                String objectType = (String) map.get("objectType");
                if ("SUBNET".equals(objectType)) {
                    TopoSubnet subnet = convertToSubnet(map);
                    subnet.setCreatedTime(now);
                    subnet.setUpdatedTime(now);
                    subnet.setSyncTime(now);
                    subnets.add(subnet);
                } else {
                    TopoNe ne = convertToNe(map);
                    ne.setCreatedTime(now);
                    ne.setUpdatedTime(now);
                    ne.setSyncTime(now);
                    nes.add(ne);
                }
            }
        }

        // 批量插入
        if (!subnets.isEmpty()) {
            subnetMapper.batchInsert(subnets);
            log.info("批量插入 {} 个子网", subnets.size());
        }

        if (!nes.isEmpty()) {
            neMapper.batchInsert(nes);
            log.info("批量插入 {} 个网元", nes.size());
        }

        insertQueue.clear();
    }

    /**
     * 转换为子网实体
     */
    private TopoSubnet convertToSubnet(Map<String, Object> data) {
        TopoSubnet subnet = new TopoSubnet();
        subnet.setDn((String) data.get("dn"));
        subnet.setName((String) data.get("name"));
        subnet.setDisplayName((String) data.get("displayName"));
        subnet.setParentDn((String) data.get("parentDn"));
        subnet.setParentType("SUBNET");
        subnet.setLayer(0);
        subnet.setAddress((String) data.get("address"));
        subnet.setLocation((String) data.get("location"));
        subnet.setMaintainer((String) data.get("maintainer"));
        subnet.setContact((String) data.get("contact"));
        return subnet;
    }

    /**
     * 转换为网元实体
     */
    private TopoNe convertToNe(Map<String, Object> data) {
        TopoNe ne = new TopoNe();
        ne.setDn((String) data.get("dn"));
        ne.setName((String) data.get("name"));
        ne.setDisplayName((String) data.get("displayName"));
        ne.setNeType(detectNeType(data));
        ne.setParentDn((String) data.get("parentDn"));
        ne.setParentType("SUBNET");
        ne.setAddress((String) data.get("address"));
        ne.setLocation((String) data.get("location"));
        ne.setMaintainer((String) data.get("maintainer"));
        ne.setContact((String) data.get("contact"));
        ne.setStatus(1);
        return ne;
    }

    /**
     * 识别网元类型
     */
    private String detectNeType(Map<String, Object> data) {
        String name = (String) data.get("name");
        if (name == null) {
            name = "";
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
