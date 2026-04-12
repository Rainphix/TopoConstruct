package com.topology.phytopo.entity;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

/**
 * 拓扑配置实体
 */
@Data
public class TopoConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 配置键 */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 配置描述 */
    private String configDesc;

    /** 创建时间戳 */
    private Long createdTime;

    /** 更新时间戳 */
    private Long updatedTime;
}
