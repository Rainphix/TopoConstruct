package com.topology.phytopo.entity;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

/**
 * 拓扑子网实体
 */
@Data
public class TopoSubnet implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 唯一标识(来自EAM) */
    private String dn;

    /** 子网名称 */
    private String name;

    /** 显示名称 */
    private String displayName;

    /** 父节点DN */
    private String parentDn;

    /** 父节点类型: SUBNET/NE */
    private String parentType;

    /** 层级深度 */
    private Integer layer;

    /** 是否为同类型合并组 */
    private Boolean isMergeGroup;

    /** 合并的设备类型 */
    private String mergeType;

    /** MED节点标识 */
    private String medNode;

    /** 地址 */
    private String address;

    /** 位置 */
    private String location;

    /** 维护人 */
    private String maintainer;

    /** 联系方式 */
    private String contact;

    /** 告警状态 */
    private Integer alarmStatus;

    /** 创建时间戳 */
    private Long createdTime;

    /** 更新时间戳 */
    private Long updatedTime;

    /** 同步时间 */
    private Long syncTime;

    /** 版本号 */
    private String version;
}
