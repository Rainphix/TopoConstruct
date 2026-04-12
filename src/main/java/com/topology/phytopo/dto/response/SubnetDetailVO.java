package com.topology.phytopo.dto.response;

import lombok.Data;

/**
 * 子网详情VO - 用于右侧面板展示
 */
@Data
public class SubnetDetailVO {

    /** 子网DN */
    private String dn;

    /** 名称 */
    private String name;

    /** 显示名称 */
    private String displayName;

    /** 层级 */
    private Integer layer;

    /** 是否为合并组 */
    private Boolean isMergeGroup;

    /** 合并的设备类型 */
    private String mergeType;

    // ========== 统计信息 ==========

    /** 网元总数 */
    private Integer neCount;

    /** 子网数量 */
    private Integer subnetCount;

    /** 离线设备数 */
    private Integer offlineCount;

    /** 在线设备数 */
    private Integer onlineCount;

    // ========== 告警统计 ==========

    /** 严重告警数 */
    private Integer criticalCount;

    /** 主要告警数 */
    private Integer majorCount;

    /** 次要告警数 */
    private Integer minorCount;

    /** 警告数 */
    private Integer warningCount;

    // ========== 基础信息 ==========

    private String address;
    private String location;
    private String maintainer;
    private String contact;
}
