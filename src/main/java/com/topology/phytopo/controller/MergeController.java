package com.topology.phytopo.controller;

import com.topology.phytopo.common.Result;
import com.topology.phytopo.dto.request.MergeConfigRequest;
import com.topology.phytopo.service.MergeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 同类型合并配置控制器
 */
@RestController
@RequestMapping("/merge")
@RequiredArgsConstructor
@Tag(name = "Merge", description = "同类型合并接口")
public class MergeController {

    private final MergeService mergeService;

    @GetMapping("/config")
    @Operation(summary = "获取合并配置", description = "获取当前的合并配置信息")
    @ApiResponse(responseCode = "200", description = "获取成功")
    public Result<MergeConfigRequest> getConfig() {
        return Result.success(mergeService.getConfig());
    }

    @PutMapping("/config")
    @Operation(summary = "更新合并配置", description = "更新合并配置，更改配置后可能自动执行或取消合并")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @ApiResponse(responseCode = "400", description = "参数校验失败")
    public Result<String> updateConfig(
            @Parameter(description = "合并配置请求")
            @RequestBody MergeConfigRequest request) {
        mergeService.updateConfig(request);
        return Result.success("配置更新成功");
    }

    @PostMapping("/execute")
    @Operation(summary = "执行合并", description = "对现有数据执行合并操作")
    @ApiResponse(responseCode = "200", description = "执行成功")
    public Result<String> executeMerge() {
        mergeService.enableMerge();
        return Result.success("合并执行成功");
    }

    @PostMapping("/disable")
    @Operation(summary = "取消合并", description = "取消所有合并组，将设备恢复到原位置")
    @ApiResponse(responseCode = "200", description = "取消成功")
    public Result<String> disableMerge() {
        mergeService.disableMerge();
        return Result.success("合并已取消");
    }
}
