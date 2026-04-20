package com.topology.phytopo.kafka;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

/**
 * 告警变更消息
 */
@Data
public class AlarmChangeMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 变更类型: ALARM_NEW / ALARM_CLEARED / ALARM_ACKED / ALARM_STATS_SNAPSHOT */
    private String changeType;

    /** 网元DN */
    private String elementDn;

    /** 告警级别: CRITICAL / MAJOR / MINOR / WARNING */
    private String alarmLevel;

    /** 告警数量变化量（增量） */
    private Integer deltaCount;

    /** 全量快照 - 严重告警数（仅 ALARM_STATS_SNAPSHOT 使用） */
    private Integer criticalCount;

    /** 全量快照 - 主要告警数（仅 ALARM_STATS_SNAPSHOT 使用） */
    private Integer majorCount;

    /** 全量快照 - 次要告警数（仅 ALARM_STATS_SNAPSHOT 使用） */
    private Integer minorCount;

    /** 全量快照 - 警告告警数（仅 ALARM_STATS_SNAPSHOT 使用） */
    private Integer warningCount;

    /** 全量快照 - 已清除告警数（仅 ALARM_STATS_SNAPSHOT 使用） */
    private Integer clearedCount;

    /** 时间戳 */
    private Long timestamp;
}
