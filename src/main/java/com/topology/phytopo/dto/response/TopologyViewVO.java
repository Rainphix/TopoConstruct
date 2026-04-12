package com.topology.phytopo.dto.response;

import lombok.Data;
import java.util.List;

/**
 * 拓扑视图VO - 用于拓扑图展示
 */
@Data
public class TopologyViewVO {
    /** 当前子网DN */
    private String subnetDn;
    /** 当前子网名称 */
    private String subnetName;
    /** 当前子网聚合告警 - 严重 */
    private Integer criticalCount;
    /** 当前子网聚合告警 - 主要 */
    private Integer majorCount;
    /** 当前子网聚合告警 - 次要 */
    private Integer minorCount;
    /** 当前子网聚合告警 - 警告 */
    private Integer warningCount;
    /** 网元总数 */
    private Integer neCount;
    /** 在线数 */
    private Integer onlineCount;
    /** 离线数 */
    private Integer offlineCount;
    /** 子网数 */
    private Integer subnetCount;
    /** 子网下的元素列表 */
    private List<TopoElement> elements;

    @Data
    public static class TopoElement {
        /** 元素DN */
        private String dn;
        /** 名称 */
        private String name;
        /** 类型: SUBNET/NE */
        private String type;
        /** 网元类型(仅NE) */
        private String neType;
        /** 图标 */
        private String icon;
        /** X坐标 */
        private Integer x;
        /** Y坐标 */
        private Integer y;
        /** 宽度 */
        private Integer width;
        /** 高度 */
        private Integer height;
        /** 状态 */
        private Integer status;
        /** 是否为合并组 */
        private Boolean isMergeGroup;
        /** 告警状态 */
        private Integer alarmStatus;
        /** 严重告警数 */
        private Integer criticalCount;
        /** 主要告警数 */
        private Integer majorCount;
        /** 次要告警数 */
        private Integer minorCount;
        /** 警告数 */
        private Integer warningCount;
        /** 子元素数量 */
        private Integer childCount;
    }
}
