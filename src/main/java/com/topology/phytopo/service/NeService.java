package com.topology.phytopo.service;

import com.topology.phytopo.dto.request.NeCreateRequest;
import com.topology.phytopo.dto.request.NeUpdateRequest;
import com.topology.phytopo.dto.response.NeDetailVO;
import com.topology.phytopo.entity.TopoAlarmStats;
import com.topology.phytopo.entity.TopoNe;
import com.topology.phytopo.mapper.TopoAlarmStatsMapper;
import com.topology.phytopo.mapper.TopoNeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 网元服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NeService {

    private final TopoNeMapper neMapper;
    private final TopoAlarmStatsMapper alarmStatsMapper;

    /**
     * 创建网元
     */
    @Transactional(rollbackFor = Exception.class)
    public TopoNe create(NeCreateRequest request) {
        TopoNe ne = new TopoNe();
        ne.setDn("DN=" + UUID.randomUUID().toString());
        ne.setName(request.getName());
        ne.setDisplayName(request.getDisplayName() != null ? request.getDisplayName() : request.getName());
        ne.setNeType(request.getNeType());
        ne.setParentDn(request.getParentDn());
        ne.setParentType("SUBNET");
        ne.setRootSubnetDn(request.getParentDn());
        ne.setAddress(request.getAddress());
        ne.setLocation(request.getLocation());
        ne.setMaintainer(request.getMaintainer());
        ne.setContact(request.getContact());
        ne.setStatus(0);
        ne.setCreatedTime(System.currentTimeMillis());
        ne.setUpdatedTime(System.currentTimeMillis());
        neMapper.insert(ne);
        log.info("创建网元成功: dn={}, name={}, type={}", ne.getDn(), request.getName(), request.getNeType());
        return ne;
    }

    /**
     * 更新网元（从请求DTO）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateFromRequest(String dn, NeUpdateRequest request) {
        TopoNe ne = neMapper.selectByDn(dn);
        if (ne == null) {
            throw new IllegalArgumentException("网元不存在: " + dn);
        }
        if (request.getName() != null) ne.setName(request.getName());
        if (request.getDisplayName() != null) ne.setDisplayName(request.getDisplayName());
        if (request.getAddress() != null) ne.setAddress(request.getAddress());
        if (request.getLocation() != null) ne.setLocation(request.getLocation());
        if (request.getMaintainer() != null) ne.setMaintainer(request.getMaintainer());
        if (request.getContact() != null) ne.setContact(request.getContact());
        ne.setUpdatedTime(System.currentTimeMillis());
        neMapper.update(ne);
        log.info("更新网元成功: dn={}", dn);
    }

    /**
     * 根据DN获取网元
     */
    public TopoNe getByDn(String dn) {
        return neMapper.selectByDn(dn);
    }

    /**
     * 获取网元详情
     */
    public NeDetailVO getDetail(String dn) {
        TopoNe ne = neMapper.selectByDn(dn);
        if (ne == null) {
            throw new IllegalArgumentException("网元不存在: " + dn);
        }
        return convertToDetailVO(ne);
    }

    /**
     * 获取子网下的所有网元
     */
    public List<TopoNe> getByParentDn(String parentDn) {
        return neMapper.selectByParentDn(parentDn);
    }

    /**
     * 更新网元状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String dn, Integer status) {
        TopoNe ne = neMapper.selectByDn(dn);
        if (ne == null) {
            throw new IllegalArgumentException("网元不存在: " + dn);
        }
        ne.setStatus(status);
        ne.setUpdatedTime(System.currentTimeMillis());
        neMapper.update(ne);
        log.info("更新网元状态: dn={}, status={}", dn, status);
    }

    /**
     * 批量更新网元父节点
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateParent(List<String> dnList, String newParentDn) {
        neMapper.batchUpdateParentDn(dnList, newParentDn, System.currentTimeMillis());
        log.info("批量更新网元父节点: count={}, newParentDn={}", dnList.size(), newParentDn);
    }

    /**
     * 删除网元
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String dn) {
        neMapper.deleteByDn(dn);
        log.info("删除网元: dn={}", dn);
    }

    /**
     * 批量删除网元
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<String> dnList) {
        neMapper.batchDeleteByDn(dnList);
        log.info("批量删除网元: count={}", dnList.size());
    }

    /**
     * 按类型统计网元数量
     */
    public int countByNeType(String neType) {
        return neMapper.countByNeType(neType);
    }

    // ========== 私有方法 ==========

    private NeDetailVO convertToDetailVO(TopoNe ne) {
        NeDetailVO vo = new NeDetailVO();
        vo.setDn(ne.getDn());
        vo.setName(ne.getName());
        vo.setDisplayName(ne.getDisplayName());
        vo.setNeType(ne.getNeType());
        vo.setIcon(getIconByNeType(ne.getNeType()));
        vo.setParentDn(ne.getParentDn());
        vo.setRootSubnetDn(ne.getRootSubnetDn());
        vo.setStatus(ne.getStatus());
        vo.setStatusDesc(ne.getStatus() != null && ne.getStatus() == 1 ? "在线" : "离线");
        vo.setAddress(ne.getAddress());
        vo.setLocation(ne.getLocation());
        vo.setMaintainer(ne.getMaintainer());
        vo.setContact(ne.getContact());

        // 获取告警统计
        TopoAlarmStats alarmStats = alarmStatsMapper.selectByElementDn(ne.getDn());
        if (alarmStats != null) {
            vo.setCriticalCount(alarmStats.getCriticalCount());
            vo.setMajorCount(alarmStats.getMajorCount());
            vo.setMinorCount(alarmStats.getMinorCount());
            vo.setWarningCount(alarmStats.getWarningCount());
        } else {
            vo.setCriticalCount(0);
            vo.setMajorCount(0);
            vo.setMinorCount(0);
            vo.setWarningCount(0);
        }

        return vo;
    }

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
}
