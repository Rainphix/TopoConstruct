package com.topology.phytopo.service;

import com.topology.phytopo.dto.request.PositionSaveRequest;
import com.topology.phytopo.dto.response.TopologyViewVO;
import com.topology.phytopo.entity.TopoNe;
import com.topology.phytopo.entity.TopoPosition;
import com.topology.phytopo.entity.TopoSubnet;
import com.topology.phytopo.entity.TopoAlarmStats;
import com.topology.phytopo.mapper.TopoNeMapper;
import com.topology.phytopo.mapper.TopoPositionMapper;
import com.topology.phytopo.mapper.TopoSubnetMapper;
import com.topology.phytopo.mapper.TopoAlarmStatsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 拓扑视图服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TopologyService {

    private final TopoSubnetMapper subnetMapper;
    private final TopoNeMapper neMapper;
    private final TopoPositionMapper positionMapper;
    private final TopoAlarmStatsMapper alarmStatsMapper;

    /**
     * 获取拓扑视图
     */
    public TopologyViewVO getView(String subnetDn) {
        TopoSubnet subnet = subnetMapper.selectByDn(subnetDn);
        if (subnet == null) {
            throw new IllegalArgumentException("子网不存在: " + subnetDn);
        }

        TopologyViewVO view = new TopologyViewVO();
        view.setSubnetDn(subnetDn);
        view.setSubnetName(subnet.getName());

        // 获取子网下的所有元素
        List<TopologyViewVO.TopoElement> elements = new ArrayList<>();

        // 1. 获取子网下的子网
        List<TopoSubnet> childSubnets = subnetMapper.selectByParentDn(subnetDn);
        for (TopoSubnet child : childSubnets) {
            TopologyViewVO.TopoElement element = new TopologyViewVO.TopoElement();
            element.setDn(child.getDn());
            element.setName(child.getName());
            element.setType("SUBNET");
            element.setIsMergeGroup(child.getIsMergeGroup());
            element.setAlarmStatus(child.getAlarmStatus());
            element.setChildCount(subnetMapper.countNeByParentDn(child.getDn()) +
                    subnetMapper.countSubnetByParentDn(child.getDn()));
            // 聚合子网下所有 NE 的告警
            fillSubnetAlarmStats(element, child.getDn());
            elements.add(element);
        }

        // 2. 获取子网下的网元
        List<TopoNe> childNes = neMapper.selectByParentDn(subnetDn);
        for (TopoNe ne : childNes) {
            TopologyViewVO.TopoElement element = new TopologyViewVO.TopoElement();
            element.setDn(ne.getDn());
            element.setName(ne.getName());
            element.setType("NE");
            element.setNeType(ne.getNeType());
            element.setIcon(getIconByNeType(ne.getNeType()));
            element.setStatus(ne.getStatus());
            element.setAlarmStatus(ne.getAlarmStatus());
            // 查询 NE 自身的告警统计
            fillNeAlarmStats(element, ne.getDn());
            elements.add(element);
        }

        // 3. 获取坐标信息
        List<TopoPosition> positions = positionMapper.selectBySubnetDn(subnetDn);
        Map<String, TopoPosition> positionMap = positions.stream()
                .collect(Collectors.toMap(TopoPosition::getElementDn, Function.identity()));

        // 收集没有坐标的元素，自动分配网格布局
        List<TopologyViewVO.TopoElement> unpositioned = new ArrayList<>();
        for (TopologyViewVO.TopoElement element : elements) {
            TopoPosition pos = positionMap.get(element.getDn());
            if (pos != null) {
                element.setX(pos.getPosX());
                element.setY(pos.getPosY());
                element.setWidth(pos.getWidth());
                element.setHeight(pos.getHeight());
            } else {
                unpositioned.add(element);
            }
        }

        if (!unpositioned.isEmpty()) {
            int gapX = 200;
            int gapY = 150;
            int cols = 5;
            // 已有坐标的元素中找最大 Y，从其下方开始布局
            int maxY = elements.stream()
                    .filter(e -> e.getX() != null && e.getY() != null
                            && (e.getX() != 0 || e.getY() != 0))
                    .mapToInt(TopologyViewVO.TopoElement::getY)
                    .max().orElse(0);
            int startRow = maxY > 0 ? (maxY + gapY + 50) / gapY : 0;

            for (int i = 0; i < unpositioned.size(); i++) {
                TopologyViewVO.TopoElement element = unpositioned.get(i);
                int col = i % cols;
                int row = startRow + i / cols;
                element.setX(50 + col * gapX);
                element.setY(50 + row * gapY);
            }
        }

        view.setElements(elements);

        // 填充当前子网的聚合统计（递归收集所有子孙 NE）
        List<String> allNeDns = new ArrayList<>();
        collectAllNeDns(subnetDn, allNeDns);

        // 统计 NE 总数和在线/离线
        view.setNeCount(allNeDns.size());
        int online = 0;
        for (String neDn : allNeDns) {
            TopoNe ne = neMapper.selectByDn(neDn);
            if (ne != null && Integer.valueOf(1).equals(ne.getStatus())) online++;
        }
        view.setOnlineCount(online);
        view.setOfflineCount(allNeDns.size() - online);
        view.setSubnetCount(childSubnets.size());

        // 聚合所有子孙 NE 的告警
        if (!allNeDns.isEmpty()) {
            List<TopoAlarmStats> stats = alarmStatsMapper.selectByElementDnList(allNeDns);
            int critical = 0, major = 0, minor = 0, warning = 0;
            for (TopoAlarmStats s : stats) {
                critical += s.getCriticalCount() != null ? s.getCriticalCount() : 0;
                major += s.getMajorCount() != null ? s.getMajorCount() : 0;
                minor += s.getMinorCount() != null ? s.getMinorCount() : 0;
                warning += s.getWarningCount() != null ? s.getWarningCount() : 0;
            }
            view.setCriticalCount(critical);
            view.setMajorCount(major);
            view.setMinorCount(minor);
            view.setWarningCount(warning);
        }

        return view;
    }

    /**
     * 保存坐标
     */
    @Transactional(rollbackFor = Exception.class)
    public void savePosition(PositionSaveRequest request) {
        String subnetDn = request.getSubnetDn();

        // 验证子网存在
        if (!subnetMapper.existsByDn(subnetDn)) {
            throw new IllegalArgumentException("子网不存在: " + subnetDn);
        }

        List<TopoPosition> positions = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (PositionSaveRequest.PositionItem item : request.getPositions()) {
            TopoPosition pos = new TopoPosition();
            pos.setSubnetDn(subnetDn);
            pos.setElementDn(item.getElementDn());
            pos.setElementType(item.getElementType());
            pos.setPosX(item.getX());
            pos.setPosY(item.getY());
            pos.setWidth(item.getWidth());
            pos.setHeight(item.getHeight());
            pos.setUpdatedTime(now);
            positions.add(pos);
        }

        // 批量插入或更新
        for (TopoPosition pos : positions) {
            TopoPosition existing = positionMapper.selectBySubnetAndElement(pos.getSubnetDn(), pos.getElementDn());
            if (existing == null) {
                pos.setCreatedTime(now);
                positionMapper.insert(pos);
            } else {
                positionMapper.update(pos);
            }
        }

        log.info("保存坐标成功: subnetDn={}, count={}", subnetDn, positions.size());
    }

    /**
     * 自动布局
     */
    @Transactional(rollbackFor = Exception.class)
    public TopologyViewVO autoLayout(String subnetDn) {
        TopologyViewVO view = getView(subnetDn);
        List<TopologyViewVO.TopoElement> elements = view.getElements();

        if (elements.isEmpty()) {
            return view;
        }

        // 分离子网和网元
        List<TopologyViewVO.TopoElement> subnets = elements.stream()
                .filter(e -> "SUBNET".equals(e.getType()))
                .collect(Collectors.toList());
        List<TopologyViewVO.TopoElement> nes = elements.stream()
                .filter(e -> "NE".equals(e.getType()))
                .collect(Collectors.toList());

        // 计算布局
        int startX = 50;
        int startY = 50;
        int gapX = 200;
        int gapY = 150;
        int cols = 5;

        // 子网布局在上层
        int row = 0;
        for (int i = 0; i < subnets.size(); i++) {
            TopologyViewVO.TopoElement element = subnets.get(i);
            element.setX(startX + (i % cols) * gapX);
            element.setY(startY + row * gapY);
            if ((i + 1) % cols == 0) {
                row++;
            }
        }

        // 网元布局在下层
        int neStartY = startY + (row + 1) * gapY + 50;
        row = 0;
        for (int i = 0; i < nes.size(); i++) {
            TopologyViewVO.TopoElement element = nes.get(i);
            element.setX(startX + (i % cols) * gapX);
            element.setY(neStartY + row * gapY);
            if ((i + 1) % cols == 0) {
                row++;
            }
        }

        // 保存布局
        List<TopoPosition> positions = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (TopologyViewVO.TopoElement element : elements) {
            TopoPosition pos = new TopoPosition();
            pos.setSubnetDn(subnetDn);
            pos.setElementDn(element.getDn());
            pos.setElementType(element.getType());
            pos.setPosX(element.getX());
            pos.setPosY(element.getY());
            pos.setWidth(element.getWidth());
            pos.setHeight(element.getHeight());
            pos.setUpdatedTime(now);
            positions.add(pos);
        }

        for (TopoPosition pos : positions) {
            TopoPosition existing = positionMapper.selectBySubnetAndElement(pos.getSubnetDn(), pos.getElementDn());
            if (existing == null) {
                pos.setCreatedTime(now);
                positionMapper.insert(pos);
            } else {
                positionMapper.update(pos);
            }
        }

        log.info("自动布局完成: subnetDn={}", subnetDn);
        return view;
    }

    /**
     * 根据网元类型获取图标
     */
    private String getIconByNeType(String neType) {
        if (neType == null) {
            return "default";
        }
        return switch (neType.toUpperCase()) {
            case "FIREWALL" -> "firewall";
            case "SWITCH" -> "switch";
            case "SERVER" -> "server";
            case "STORAGE" -> "storage";
            case "GATEWAY" -> "gateway";
            case "CHASSIS" -> "chassis";
            case "RACK" -> "rack";
            default -> "default";
        };
    }

    /**
     * 填充子网元素的聚合告警统计
     */
    private void fillSubnetAlarmStats(TopologyViewVO.TopoElement element, String subnetDn) {
        List<String> allNeDns = new ArrayList<>();
        collectAllNeDns(subnetDn, allNeDns);
        aggregateAndSetAlarmStats(element, allNeDns);
    }

    /**
     * 填充 NE 元素的告警统计
     */
    private void fillNeAlarmStats(TopologyViewVO.TopoElement element, String neDn) {
        TopoAlarmStats stats = alarmStatsMapper.selectByElementDn(neDn);
        if (stats != null) {
            element.setCriticalCount(stats.getCriticalCount());
            element.setMajorCount(stats.getMajorCount());
            element.setMinorCount(stats.getMinorCount());
            element.setWarningCount(stats.getWarningCount());
        }
    }

    /**
     * 递归收集子网下所有 NE 的 DN
     */
    private void collectAllNeDns(String subnetDn, List<String> result) {
        List<TopoNe> nes = neMapper.selectByParentDn(subnetDn);
        for (TopoNe ne : nes) {
            result.add(ne.getDn());
        }
        List<TopoSubnet> childSubnets = subnetMapper.selectByParentDn(subnetDn);
        for (TopoSubnet child : childSubnets) {
            collectAllNeDns(child.getDn(), result);
        }
    }

    /**
     * 根据 NE DN 列表批量查询告警统计并聚合到 element
     */
    private void aggregateAndSetAlarmStats(TopologyViewVO.TopoElement element, List<String> neDns) {
        if (neDns.isEmpty()) return;
        List<TopoAlarmStats> stats = alarmStatsMapper.selectByElementDnList(neDns);
        int critical = 0, major = 0, minor = 0, warning = 0;
        for (TopoAlarmStats s : stats) {
            critical += s.getCriticalCount() != null ? s.getCriticalCount() : 0;
            major += s.getMajorCount() != null ? s.getMajorCount() : 0;
            minor += s.getMinorCount() != null ? s.getMinorCount() : 0;
            warning += s.getWarningCount() != null ? s.getWarningCount() : 0;
        }
        element.setCriticalCount(critical);
        element.setMajorCount(major);
        element.setMinorCount(minor);
        element.setWarningCount(warning);
    }
}
