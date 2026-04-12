package com.topology.phytopo.entity;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

/**
 * 拓扑告警统计实体
 */
@Data
public class TopoAlarmStats implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 元素DN */
    private String elementDn;

    /** 元素类型: SUBNET/NE */
    private String elementType;

    /** 严重告警数 */
    private Integer criticalCount;

    /** 主要告警数 */
    private Integer majorCount;

    /** 次要告警数 */
    private Integer minorCount;

    /** 警告告警数 */
    private Integer warningCount;

    /** 已清除告警数 */
    private Integer clearedCount;

    /** 总告警数 */
    private Integer totalCount;

    /** 更新时间戳 */
    private Long updatedTime;
}
