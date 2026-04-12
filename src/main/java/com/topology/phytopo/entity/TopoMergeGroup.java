package com.topology.phytopo.entity;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

/**
 * 拓扑合并组实体
 */
@Data
public class TopoMergeGroup implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 合并组子网DN */
    private String subnetDn;

    /** 父子网DN */
    private String parentDn;

    /** 合并的网元类型 */
    private String neType;

    /** 组编号 */
    private Integer groupIndex;

    /** 成员数量 */
    private Integer memberCount;

    /** 创建时间戳 */
    private Long createdTime;

    /** 更新时间戳 */
    private Long updatedTime;
}
