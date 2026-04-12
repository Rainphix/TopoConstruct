package com.topology.phytopo.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 合并配置请求
 */
@Data
public class MergeConfigRequest {

    /** 是否开启合并 */
    private Boolean enabled;

    /** 合并阈值 */
    @Min(value = 1, message = "阈值必须大于0")
    private Integer threshold;

    /** 合并模式: THRESHOLD / ALL */
    @NotNull
    private String mode;
}
