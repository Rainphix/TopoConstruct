package com.topology.phytopo.controller;

import com.topology.phytopo.common.Result;
import com.topology.phytopo.dto.request.*;
import com.topology.phytopo.dto.response.SubnetDetailVO;
import com.topology.phytopo.dto.response.TreeNodeVO;
import com.topology.phytopo.service.SubnetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 子网管理控制器
 */
@RestController
@RequestMapping("/subnet")
@RequiredArgsConstructor
@Tag(name = "Subnet", description = "子网管理接口")
@Slf4j
public class SubnetController {

    private final SubnetService subnetService;

    @PostMapping
    @Operation(summary = "创建子网", description = "创建新的子网节点")
    public Result<String> create(
            @Parameter(description = "子网创建请求")
            @Valid @RequestBody SubnetCreateRequest request) {
        subnetService.create(request);
        return Result.success("创建成功");
    }

    @PutMapping("/update")
    @Operation(summary = "更新子网", description = "更新指定子网信息")
    public Result<String> update(
            @Parameter(description = "子网DN", required = true)
            @RequestParam String dn,
            @Parameter(description = "子网更新请求")
            @Valid @RequestBody SubnetUpdateRequest request) {
        subnetService.update(dn, request);
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除子网", description = "删除指定子网，子网下存在设备或子网时无法删除")
    public Result<String> delete(
            @Parameter(description = "子网DN", required = true)
            @RequestParam String dn) {
        subnetService.delete(dn);
        return Result.success("删除成功");
    }

    @GetMapping("/detail")
    @Operation(summary = "获取子网详情", description = "获取指定子网的详细信息")
    public Result<SubnetDetailVO> getDetail(
            @Parameter(description = "子网DN", required = true)
            @RequestParam String dn) {
        return Result.success(subnetService.getDetail(dn));
    }

    @GetMapping("/tree")
    @Operation(summary = "获取子网树", description = "获取子网树结构，可指定根节点")
    public Result<List<TreeNodeVO>> getTree(
            @RequestParam(required = false) String rootDn) {
        return Result.success(subnetService.getTree(rootDn));
    }

    @PostMapping("/batch-move")
    @Operation(summary = "批量移入设备", description = "将多个设备批量移入指定子网")
    public Result<String> batchMove(
            @Parameter(description = "批量移入请求")
            @Valid @RequestBody BatchMoveRequest request) {
        subnetService.batchMove(request);
        return Result.success("移入成功");
    }

    @PostMapping("/move")
    @Operation(summary = "移动节点", description = "将网元或子网移动到目标子网下")
    public Result<String> move(
            @Parameter(description = "被移动节点的DN", required = true)
            @RequestParam String dn,
            @Parameter(description = "节点类型: SUBNET 或 NE", required = true)
            @RequestParam String type,
            @Parameter(description = "目标子网的DN", required = true)
            @RequestParam String targetDn) {
        subnetService.move(dn, type, targetDn);
        return Result.success("移动成功");
    }
}
