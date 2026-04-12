package com.topology.phytopo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 坐标保存请求
 */
@Data
public class PositionSaveRequest {

    @NotBlank(message = "子网DN不能为空")
    private String subnetDn;

    @NotNull(message = "坐标列表不能为空")
    private List<PositionItem> positions;

    @Data
    public static class PositionItem {
        @NotBlank(message = "元素DN不能为空")
        private String elementDn;

        @NotBlank(message = "元素类型不能为空")
        private String elementType;

        @NotNull(message = "X坐标不能为空")
        private Integer x;

        @NotNull(message = "Y坐标不能为空")
        private Integer y;

        private Integer width;
        private Integer height;
    }
}
