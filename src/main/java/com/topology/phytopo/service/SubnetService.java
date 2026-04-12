package com.topology.phytopo.service;

import com.topology.phytopo.dto.request.BatchMoveRequest;
import com.topology.phytopo.dto.request.SubnetCreateRequest;
import com.topology.phytopo.dto.request.SubnetUpdateRequest;
import com.topology.phytopo.dto.response.SubnetDetailVO;
import com.topology.phytopo.dto.response.TreeNodeVO;
import com.topology.phytopo.entity.TopoNe;
import com.topology.phytopo.entity.TopoSubnet;
import com.topology.phytopo.entity.TopoAlarmStats;
import com.topology.phytopo.mapper.TopoNeMapper;
import com.topology.phytopo.mapper.TopoSubnetMapper;
import com.topology.phytopo.mapper.TopoAlarmStatsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 子网服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubnetService {

    private final TopoSubnetMapper subnetMapper;
    private final TopoNeMapper neMapper;
    private final TopoAlarmStatsMapper alarmStatsMapper;

    /**
     * 创建子网
     */
    @Transactional(rollbackFor = Exception.class)
    public TopoSubnet create(SubnetCreateRequest request) {
        // 生成DN
        String dn = generateDn(request.getParentDn());

        // 计算层级
        int layer = calculateLayer(request.getParentDn());

        // 构建实体
        TopoSubnet subnet = new TopoSubnet();
        subnet.setDn(dn);
        subnet.setName(request.getName());
        subnet.setDisplayName(request.getDisplayName());
        subnet.setParentDn(request.getParentDn());
        subnet.setParentType("SUBNET");
        subnet.setLayer(layer);
        subnet.setIsMergeGroup(false);
        subnet.setAddress(request.getAddress());
        subnet.setLocation(request.getLocation());
        subnet.setMaintainer(request.getMaintainer());
        subnet.setContact(request.getContact());
        subnet.setCreatedTime(System.currentTimeMillis());
        subnet.setUpdatedTime(System.currentTimeMillis());

        // 保存
        subnetMapper.insert(subnet);

        log.info("创建子网成功: dn={}, name={}", dn, request.getName());
        return subnet;
    }

    /**
     * 更新子网
     */
    @Transactional(rollbackFor = Exception.class)
    public TopoSubnet update(String dn, SubnetUpdateRequest request) {
        TopoSubnet subnet = subnetMapper.selectByDn(dn);
        if (subnet == null) {
            throw new IllegalArgumentException("子网不存在: " + dn);
        }

        subnet.setName(request.getName());
        subnet.setDisplayName(request.getDisplayName());
        subnet.setAddress(request.getAddress());
        subnet.setLocation(request.getLocation());
        subnet.setMaintainer(request.getMaintainer());
        subnet.setContact(request.getContact());
        subnet.setUpdatedTime(System.currentTimeMillis());

        subnetMapper.update(subnet);
        log.info("更新子网成功: dn={}", dn);
        return subnet;
    }

    /**
     * 删除子网
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String dn) {
        TopoSubnet subnet = subnetMapper.selectByDn(dn);
        if (subnet == null) {
            throw new IllegalArgumentException("子网不存在: " + dn);
        }

        // 如果是合并组子网，允许直接删除
        if (Boolean.TRUE.equals(subnet.getIsMergeGroup())) {
            subnetMapper.deleteByDn(dn);
            log.info("删除合并组子网: dn={}", dn);
            return;
        }

        // 检查子网下是否有设备
        int neCount = subnetMapper.countNeByParentDn(dn);
        if (neCount > 0) {
            throw new IllegalStateException("子网下存在设备，无法删除");
        }

        // 检查子网下是否有子网
        int subnetCount = subnetMapper.countSubnetByParentDn(dn);
        if (subnetCount > 0) {
            throw new IllegalStateException("子网下存在子网，无法删除");
        }

        subnetMapper.deleteByDn(dn);
        log.info("删除子网成功: dn={}", dn);
    }

    /**
     * 获取子网详情
     */
    public SubnetDetailVO getDetail(String dn) {
        TopoSubnet subnet = subnetMapper.selectByDn(dn);
        if (subnet == null) {
            throw new IllegalArgumentException("子网不存在: " + dn);
        }
        return convertToDetailVO(subnet);
    }

    /**
     * 获取子网树（递归构建完整层级结构）
     */
    public List<TreeNodeVO> getTree(String rootDn) {
        if (rootDn == null) {
            // 获取所有根子网，递归构建子树
            List<TopoSubnet> roots = subnetMapper.selectByParentDn(null);
            return roots.stream().map(root -> buildTreeNodeRecursive(root, null)).toList();
        }

        TopoSubnet root = subnetMapper.selectByDn(rootDn);
        if (root == null) {
            throw new IllegalArgumentException("子网不存在: " + rootDn);
        }

        List<TreeNodeVO> tree = new ArrayList<>();
        tree.add(buildTreeNodeRecursive(root, null));
        return tree;
    }

    /**
     * 获取子网下的子节点
     */
    public List<TreeNodeVO> getChildren(String parentDn) {
        List<TopoSubnet> subnets = subnetMapper.selectByParentDn(parentDn);
        List<TopoNe> nes = neMapper.selectByParentDn(parentDn);

        List<TreeNodeVO> children = new ArrayList<>();
        children.addAll(subnets.stream().map(s -> buildTreeNodeRecursive(s, null)).toList());
        children.addAll(nes.stream().map(this::convertNeToTreeNode).toList());
        return children;
    }

    /**
     * 批量移入设备
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchMove(BatchMoveRequest request) {
        String targetSubnetDn = request.getTargetSubnetDn();
        List<String> neDnList = request.getNeDnList();

        // 验证目标子网
        TopoSubnet targetSubnet = subnetMapper.selectByDn(targetSubnetDn);
        if (targetSubnet == null) {
            throw new IllegalArgumentException("目标子网不存在: " + targetSubnetDn);
        }

        // 如果是合并组子网,检查设备类型
        if (Boolean.TRUE.equals(targetSubnet.getIsMergeGroup())) {
            String mergeType = targetSubnet.getMergeType();
            for (String neDn : neDnList) {
                TopoNe ne = neMapper.selectByDn(neDn);
                if (ne == null || !ne.getNeType().equals(mergeType)) {
                    throw new IllegalArgumentException("设备类型不匹配，不能移入合并组: " + neDn);
                }
            }
        }

        // 批量更新父节点
        neMapper.batchUpdateParentDn(neDnList, targetSubnetDn, System.currentTimeMillis());
        log.info("批量移入设备成功: targetSubnetDn={}, count={}", targetSubnetDn, neDnList.size());
    }

    // ========== 私有方法 ==========

    /**
     * 移动网元或子网到目标子网
     */
    @Transactional(rollbackFor = Exception.class)
    public void move(String dn, String type, String targetDn) {
        // 不能移动到自己
        if (dn.equals(targetDn)) {
            throw new IllegalArgumentException("不能移动到自身");
        }

        // 验证目标子网存在
        TopoSubnet targetSubnet = subnetMapper.selectByDn(targetDn);
        if (targetSubnet == null) {
            throw new IllegalArgumentException("目标子网不存在: " + targetDn);
        }

        if ("NE".equals(type)) {
            moveNe(dn, targetSubnet);
        } else if ("SUBNET".equals(type)) {
            moveSubnet(dn, targetSubnet);
        } else {
            throw new IllegalArgumentException("无效的类型: " + type);
        }
    }

    private void moveNe(String dn, TopoSubnet targetSubnet) {
        TopoNe ne = neMapper.selectByDn(dn);
        if (ne == null) {
            throw new IllegalArgumentException("网元不存在: " + dn);
        }

        // 计算新的根子网DN：沿目标子网向上找到根
        String rootSubnetDn = findRootSubnetDn(targetSubnet);

        neMapper.updateParentDn(dn, targetSubnet.getDn(), rootSubnetDn, System.currentTimeMillis());
        log.info("移动网元成功: dn={}, targetSubnet={}", dn, targetSubnet.getDn());
    }

    private void moveSubnet(String dn, TopoSubnet targetSubnet) {
        TopoSubnet subnet = subnetMapper.selectByDn(dn);
        if (subnet == null) {
            throw new IllegalArgumentException("子网不存在: " + dn);
        }

        // 防止循环：检查目标子网不是被移动子网的子孙节点
        if (isDescendant(dn, targetSubnet.getDn())) {
            throw new IllegalArgumentException("不能移动到自身或自身的子节点下");
        }

        int newLayer = targetSubnet.getLayer() + 1;
        subnetMapper.updateParentDn(dn, targetSubnet.getDn(), newLayer, System.currentTimeMillis());
        log.info("移动子网成功: dn={}, targetSubnet={}, newLayer={}", dn, targetSubnet.getDn(), newLayer);
    }

    /** 沿子网向上查找根子网DN */
    private String findRootSubnetDn(TopoSubnet subnet) {
        TopoSubnet current = subnet;
        while (current.getParentDn() != null) {
            current = subnetMapper.selectByDn(current.getParentDn());
            if (current == null) break;
        }
        return current != null ? current.getDn() : subnet.getDn();
    }

    /** 检查 targetDn 是否是 parentDn 的子孙节点 */
    private boolean isDescendant(String parentDn, String targetDn) {
        if (parentDn.equals(targetDn)) return true;
        List<TopoSubnet> children = subnetMapper.selectByParentDn(parentDn);
        for (TopoSubnet child : children) {
            if (isDescendant(child.getDn(), targetDn)) {
                return true;
            }
        }
        return false;
    }

    private String generateDn(String parentDn) {
        return "DN=" + UUID.randomUUID().toString();
    }

    private int calculateLayer(String parentDn) {
        if (parentDn == null) {
            return 0;
        }
        TopoSubnet parent = subnetMapper.selectByDn(parentDn);
        if (parent == null) {
            return 0;
        }
        return parent.getLayer() + 1;
    }

    /**
     * 递归构建树节点（包含子网和网元子节点）
     * @param maxDepth 递归深度限制，null 表示不限制
     */
    private TreeNodeVO buildTreeNodeRecursive(TopoSubnet subnet, Integer maxDepth) {
        TreeNodeVO node = new TreeNodeVO();
        node.setDn(subnet.getDn());
        node.setName(subnet.getName());
        node.setDisplayName(subnet.getDisplayName());
        node.setType("SUBNET");
        node.setIsMergeGroup(subnet.getIsMergeGroup());
        node.setLayer(subnet.getLayer());

        // 查询子子网和网元
        List<TopoSubnet> childSubnets = subnetMapper.selectByParentDn(subnet.getDn());
        List<TopoNe> childNes = neMapper.selectByParentDn(subnet.getDn());

        int childCount = childSubnets.size() + childNes.size();
        node.setChildCount(childCount);

        int alarmTotal = 0;

        if (childCount > 0) {
            List<TreeNodeVO> children = new ArrayList<>();
            // 递归构建子子网
            for (TopoSubnet child : childSubnets) {
                TreeNodeVO childNode = buildTreeNodeRecursive(child, maxDepth);
                alarmTotal += childNode.getAlarmCount() != null ? childNode.getAlarmCount() : 0;
                children.add(childNode);
            }
            // 添加网元叶子节点
            for (TopoNe ne : childNes) {
                TreeNodeVO neNode = convertNeToTreeNode(ne);
                alarmTotal += neNode.getAlarmCount() != null ? neNode.getAlarmCount() : 0;
                children.add(neNode);
            }
            node.setChildren(children);
        }

        node.setAlarmCount(alarmTotal);
        return node;
    }

    /**
     * 将 TopoNe 转换为 TreeNodeVO（叶子节点）
     */
    private TreeNodeVO convertNeToTreeNode(TopoNe ne) {
        TreeNodeVO node = new TreeNodeVO();
        node.setDn(ne.getDn());
        node.setName(ne.getName());
        node.setDisplayName(ne.getDisplayName());
        node.setType("NE");
        node.setNeType(ne.getNeType());
        node.setLayer(null);
        node.setIsMergeGroup(false);
        node.setStatus(ne.getStatus());
        node.setChildCount(0);

        // 查询 NE 自身的告警总数
        TopoAlarmStats stats = alarmStatsMapper.selectByElementDn(ne.getDn());
        int alarmCount = 0;
        if (stats != null) {
            alarmCount += stats.getCriticalCount() != null ? stats.getCriticalCount() : 0;
            alarmCount += stats.getMajorCount() != null ? stats.getMajorCount() : 0;
            alarmCount += stats.getMinorCount() != null ? stats.getMinorCount() : 0;
            alarmCount += stats.getWarningCount() != null ? stats.getWarningCount() : 0;
        }
        node.setAlarmCount(alarmCount);

        return node;
    }

    private TreeNodeVO convertToTreeNode(TopoSubnet subnet) {
        TreeNodeVO node = new TreeNodeVO();
        node.setDn(subnet.getDn());
        node.setName(subnet.getName());
        node.setDisplayName(subnet.getDisplayName());
        node.setType("SUBNET");
        node.setIsMergeGroup(subnet.getIsMergeGroup());
        node.setLayer(subnet.getLayer());
        node.setChildCount(subnetMapper.countNeByParentDn(subnet.getDn()) +
                subnetMapper.countSubnetByParentDn(subnet.getDn()));
        return node;
    }

    private SubnetDetailVO convertToDetailVO(TopoSubnet subnet) {
        SubnetDetailVO vo = new SubnetDetailVO();
        vo.setDn(subnet.getDn());
        vo.setName(subnet.getName());
        vo.setDisplayName(subnet.getDisplayName());
        vo.setLayer(subnet.getLayer());
        vo.setIsMergeGroup(subnet.getIsMergeGroup());
        vo.setAddress(subnet.getAddress());
        vo.setLocation(subnet.getLocation());
        vo.setMaintainer(subnet.getMaintainer());
        vo.setContact(subnet.getContact());

        // 递归收集子网下所有 NE 的 DN
        List<String> allNeDns = new ArrayList<>();
        collectAllNeDns(subnet.getDn(), allNeDns);

        // 统计 NE 数量、在线/离线
        List<TopoNe> allNes = allNeDns.stream()
                .map(neMapper::selectByDn)
                .filter(java.util.Objects::nonNull)
                .toList();
        int onlineCount = (int) allNes.stream().filter(ne -> Integer.valueOf(1).equals(ne.getStatus())).count();
        vo.setNeCount(allNes.size());
        vo.setOnlineCount(onlineCount);
        vo.setOfflineCount(allNes.size() - onlineCount);

        // 统计直接子网数量
        List<TopoSubnet> childSubnets = subnetMapper.selectByParentDn(subnet.getDn());
        vo.setSubnetCount(childSubnets.size());

        // 批量查询告警统计并聚合
        int critical = 0, major = 0, minor = 0, warning = 0;
        if (!allNeDns.isEmpty()) {
            List<TopoAlarmStats> stats = alarmStatsMapper.selectByElementDnList(allNeDns);
            for (TopoAlarmStats s : stats) {
                critical += s.getCriticalCount() != null ? s.getCriticalCount() : 0;
                major += s.getMajorCount() != null ? s.getMajorCount() : 0;
                minor += s.getMinorCount() != null ? s.getMinorCount() : 0;
                warning += s.getWarningCount() != null ? s.getWarningCount() : 0;
            }
        }
        vo.setCriticalCount(critical);
        vo.setMajorCount(major);
        vo.setMinorCount(minor);
        vo.setWarningCount(warning);

        return vo;
    }

    /**
     * 递归收集子网下所有 NE 的 DN
     */
    private void collectAllNeDns(String subnetDn, List<String> result) {
        // 直接子 NE
        List<TopoNe> nes = neMapper.selectByParentDn(subnetDn);
        for (TopoNe ne : nes) {
            result.add(ne.getDn());
        }
        // 递归子子网
        List<TopoSubnet> childSubnets = subnetMapper.selectByParentDn(subnetDn);
        for (TopoSubnet child : childSubnets) {
            collectAllNeDns(child.getDn(), result);
        }
    }
}
