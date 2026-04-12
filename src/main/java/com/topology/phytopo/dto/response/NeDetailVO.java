package com.topology.phytopo.dto.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 网元详情VO - 用于右侧面板展示
 */
@Data
public class NeDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;
    /** 网元DN */
    private String dn;
    /** 网元名称 */
    private String name;
    /** 显示名称 */
    private String displayName;
    /** 网元类型 */
    private String neType;
    /** 图标 */
    private String icon;
    /** 父节点DN */
    private String parentDn;
    /** 父节点类型: SUBNET/NE */
    private String parentType;
    /** 所属根子网DN */
    private String rootSubnetDn;
    /** MED节点标识 */
    private String medNode;
    /** IP地址 */
    private String address;
    /** 物理位置 */
    private String location;
    /** 维护人 */
    private String maintainer;
    /** 联方式 */
    private String contact;
    /** 状态: 0-离线,1-在线 */
    private Integer status;
    /** 告警状态 */
    private Integer alarmStatus;
    /** 告警统计 */
    private Integer criticalCount;
    private Integer majorCount;
    private Integer minorCount;
    private Integer warningCount;
    /** 序列号 */
    private Integer sequenceNo;
    /** 创建时间戳 */
    private Long createdTime;
    /** 更新时间戳 */
    private Long updatedTime;
    /** 同步时间 */
    private Long syncTime;
    /** 状态描述 */
    private String statusDesc;
    /** 版本号 */
    private String version;
}
