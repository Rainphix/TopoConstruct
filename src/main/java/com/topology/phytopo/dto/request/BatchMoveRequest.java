package com.topology.phytopo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

/**
 * 批量移入设备请求
 */
@Data
public class BatchMoveRequest {

    @NotBlank(message = "目标子网DN不能为空")
    private String targetSubnetDn;

    @NotEmpty(message = "设备DN列表不能为空")
    private List<String> neDnList;
}
