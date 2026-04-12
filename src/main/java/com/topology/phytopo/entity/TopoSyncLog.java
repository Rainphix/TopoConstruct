package com.topology.phytopo.entity;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

/**
 * 同步日志实体
 */
@Data
public class TopoSyncLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 同步类型: FULL/INCREMENTAL */
    private String syncType;

    /** 同步源 */
    private String syncSource;

    /** 状态: RUNNING/SUCCESS/FAILED */
    private String status;

    /** 总处理数量 */
    private Integer totalCount;

    /** 成功数量 */
    private Integer successCount;

    /** 失败数量 */
    private Integer failedCount;

    /** 错误信息 */
    private String errorMsg;

    /** 开始时间戳 */
    private Long startTime;

    /** 结束时间戳 */
    private Long endTime;

    /** 创建时间戳 */
    private Long createdTime;
}
