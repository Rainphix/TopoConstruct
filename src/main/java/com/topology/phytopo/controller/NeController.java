package com.topology.phytopo.controller;

import com.topology.phytopo.common.Result;
import com.topology.phytopo.dto.request.NeCreateRequest;
import com.topology.phytopo.dto.request.NeUpdateRequest;
import com.topology.phytopo.dto.response.NeDetailVO;
import com.topology.phytopo.service.NeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 网元管理控制器
 */
@RestController
@RequestMapping("/ne")
@RequiredArgsConstructor
@Tag(name = "NetworkElement", description = "网元管理接口")
public class NeController {

    private final NeService neService;

    @GetMapping("/detail")
    @Operation(summary = "获取网元详情", description = "根据DN获取网元详细信息")
    public Result<NeDetailVO> getDetail(
            @Parameter(description = "网元DN", required = true)
            @RequestParam String dn) {
        return Result.success(neService.getDetail(dn));
    }

    @PostMapping
    @Operation(summary = "创建网元", description = "在指定子网下创建新的网元")
    public Result<String> create(
            @Parameter(description = "网元创建请求")
            @Valid @RequestBody NeCreateRequest request) {
        neService.create(request);
        return Result.success("创建成功");
    }

    @PutMapping("/update")
    @Operation(summary = "更新网元", description = "更新指定网元信息")
    public Result<String> update(
            @Parameter(description = "网元DN", required = true)
            @RequestParam String dn,
            @Parameter(description = "网元更新请求")
            @Valid @RequestBody NeUpdateRequest request) {
        neService.updateFromRequest(dn, request);
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除网元", description = "删除指定网元")
    public Result<String> delete(
            @Parameter(description = "网元DN", required = true)
            @RequestParam String dn) {
        neService.delete(dn);
        return Result.success("删除成功");
    }
}
