package com.topology.phytopo.controller;

import com.topology.phytopo.common.Result;
import com.topology.phytopo.dto.request.PositionSaveRequest;
import com.topology.phytopo.dto.response.TopologyViewVO;
import com.topology.phytopo.service.TopologyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 拓扑视图控制器
 */
@RestController
@RequestMapping("/topo")
@RequiredArgsConstructor
@Tag(name = "Topology", description = "拓扑视图接口")
public class TopologyController {

    private final TopologyService topologyService;

    @GetMapping("/view")
    @Operation(summary = "获取拓扑视图", description = "获取指定子网的拓扑视图，包含所有子元素及其坐标信息")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "404", description = "子网不存在")
    public Result<TopologyViewVO> getView(
            @Parameter(description = "子网DN", required = true)
            @RequestParam String subnetDn) {
        return Result.success(topologyService.getView(subnetDn));
    }

    @PostMapping("/position")
    @Operation(summary = "保存坐标", description = "批量保存元素的坐标信息")
    @ApiResponse(responseCode = "200", description = "保存成功")
    @ApiResponse(responseCode = "400", description = "参数校验失败")
    public Result<String> savePosition(
            @Parameter(description = "坐标保存请求")
            @Valid @RequestBody PositionSaveRequest request) {
        topologyService.savePosition(request);
        return Result.success("保存成功");
    }

    @PostMapping("/auto-layout")
    @Operation(summary = "自动布局", description = "对指定子网下的元素进行自动布局")
    @ApiResponse(responseCode = "200", description = "布局成功")
    @ApiResponse(responseCode = "404", description = "子网不存在")
    public Result<TopologyViewVO> autoLayout(
            @Parameter(description = "子网DN", required = true)
            @RequestParam String subnetDn) {
        return Result.success(topologyService.autoLayout(subnetDn));
    }
}
