package com.topology.phytopo.entity;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

/**
 * 拓扑坐标实体
 */
@Data
public class TopoPosition implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 所属子网视图DN */
    private String subnetDn;

    /** 元素DN */
    private String elementDn;

    /** 元素类型: SUBNET/NE */
    private String elementType;

    /** X坐标 */
    private Integer posX;

    /** Y坐标 */
    private Integer posY;

    /** 宽度 */
    private Integer width;

    /** 高度 */
    private Integer height;

    /** 创建时间戳 */
    private Long createdTime;

    /** 更新时间戳 */
    private Long updatedTime;
}
