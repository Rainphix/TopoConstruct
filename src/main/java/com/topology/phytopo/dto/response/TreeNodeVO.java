package com.topology.phytopo.dto.response;

import lombok.Data;
import java.util.List;

/**
 * 树节点VO - 用于左侧树展示
 */
@Data
public class TreeNodeVO {

    /** 节点DN */
    private String dn;

    /** 节点名称 */
    private String name;

    /** 显示名称 */
    private String displayName;

    /** 节点类型: SUBNET/NE */
    private String type;

    /** 网元类型(仅NE) */
    private String neType;

    /** 图标名称 */
    private String icon;

    /** 是否为合并组 */
    private Boolean isMergeGroup;

    /** 层级 */
    private Integer layer;

    /** 设备状态: 0-离线 1-在线 */
    private Integer status;

    /** 子节点数量 */
    private Integer childCount;

    /** 告警总数（子树聚合） */
    private Integer alarmCount;

    /** 子节点 */
    private List<TreeNodeVO> children;
}
