package com.topology.phytopo.service;

import com.topology.phytopo.entity.TopoAlarmStats;
import com.topology.phytopo.kafka.AlarmChangeMessage;
import com.topology.phytopo.mapper.TopoAlarmStatsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 告警统计服务
 * <p>
 * 支持两种并发安全的更新模式：
 * <ul>
 *   <li>增量模式（incrementAndUpdate）：适用于 ALARM_NEW / ALARM_CLEARED 等逐条事件，由 MySQL 行锁保证原子性</li>
 *   <li>乐观锁模式（updateWithVersion）：适用于全量快照覆盖，通过版本号检测并发冲突并重试</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmStatsService {

    private final TopoAlarmStatsMapper alarmStatsMapper;

    @Value("${topology.alarm.retry-times:3}")
    private int retryTimes;

    private static final Map<String, String> LEVEL_TO_FIELD = Map.of(
            "CRITICAL", "criticalCount",
            "MAJOR", "majorCount",
            "MINOR", "minorCount",
            "WARNING", "warningCount",
            "CLEARED", "clearedCount"
    );

    /**
     * 处理告警变更消息（增量模式）
     * <p>
     * 对于 ALARM_NEW / ALARM_CLEARED 事件，使用增量 SQL（INSERT ... ON DUPLICATE KEY UPDATE x = x + delta），
     * 由 MySQL 行锁保证并发安全，无需应用层加锁。
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleAlarmChange(AlarmChangeMessage message) {
        String changeType = message.getChangeType();
        log.info("处理告警变更: type={}, elementDn={}, level={}",
                changeType, message.getElementDn(), message.getAlarmLevel());

        switch (changeType) {
            case "ALARM_NEW", "ALARM_CLEARED" -> handleIncrementalChange(message);
            case "ALARM_STATS_SNAPSHOT" -> handleSnapshotChange(message);
            default -> log.warn("未知的告警变更类型: {}", changeType);
        }
    }

    /**
     * 增量更新：单条告警新增或清除
     * <p>
     * 使用 incrementAndUpdate（INSERT ... ON DUPLICATE KEY UPDATE x = x + delta），
     * 无论单实例还是多实例部署均由 MySQL 行锁保证原子性。
     */
    private void handleIncrementalChange(AlarmChangeMessage message) {
        TopoAlarmStats delta = new TopoAlarmStats();
        delta.setElementDn(message.getElementDn());
        delta.setElementType("NE");
        delta.setUpdatedTime(System.currentTimeMillis());

        int deltaCount = resolveDeltaCount(message);
        String field = LEVEL_TO_FIELD.get(message.getAlarmLevel());
        if (field == null) {
            log.warn("未知的告警级别: {}", message.getAlarmLevel());
            return;
        }

        switch (field) {
            case "criticalCount" -> delta.setCriticalCount(deltaCount);
            case "majorCount" -> delta.setMajorCount(deltaCount);
            case "minorCount" -> delta.setMinorCount(deltaCount);
            case "warningCount" -> delta.setWarningCount(deltaCount);
            case "clearedCount" -> delta.setClearedCount(deltaCount);
        }

        // totalCount 同步增减
        delta.setTotalCount(deltaCount);

        alarmStatsMapper.incrementAndUpdate(delta);
        log.info("增量更新告警统计完成: elementDn={}, level={}, delta={}",
                message.getElementDn(), message.getAlarmLevel(), deltaCount);
    }

    /**
     * 全量快照更新：带乐观锁重试
     * <p>
     * 适用于外部系统定期推送完整告警统计快照的场景。
     * 使用版本号检测并发修改，冲突时自动重试。
     */
    private void handleSnapshotChange(AlarmChangeMessage message) {
        for (int attempt = 1; attempt <= retryTimes; attempt++) {
            TopoAlarmStats current = alarmStatsMapper.selectByElementDn(message.getElementDn());

            if (current == null) {
                // 首次插入，使用 insertOrUpdate（绝对值覆盖）
                TopoAlarmStats stats = buildSnapshotStats(message);
                alarmStatsMapper.insertOrUpdate(stats);
                log.info("快照插入告警统计完成: elementDn={}", message.getElementDn());
                return;
            }

            // 带版本号的乐观锁更新
            TopoAlarmStats updated = buildSnapshotStats(message);
            updated.setVersion(current.getVersion());

            int rows = alarmStatsMapper.updateWithVersion(updated);
            if (rows > 0) {
                log.info("快照更新告警统计完成: elementDn={}", message.getElementDn());
                return;
            }

            log.warn("乐观锁冲突，第 {}/{} 次重试: elementDn={}",
                    attempt, retryTimes, message.getElementDn());
        }

        log.error("快照更新告警统计失败，重试 {} 次后仍有冲突: elementDn={}",
                retryTimes, message.getElementDn());
    }

    private int resolveDeltaCount(AlarmChangeMessage message) {
        int delta = message.getDeltaCount() != null ? message.getDeltaCount() : 1;
        if ("ALARM_CLEARED".equals(message.getChangeType())) {
            delta = -Math.abs(delta);
        }
        return delta;
    }

    private TopoAlarmStats buildSnapshotStats(AlarmChangeMessage message) {
        TopoAlarmStats stats = new TopoAlarmStats();
        stats.setElementDn(message.getElementDn());
        stats.setElementType("NE");
        stats.setCriticalCount(nullSafe(message.getCriticalCount()));
        stats.setMajorCount(nullSafe(message.getMajorCount()));
        stats.setMinorCount(nullSafe(message.getMinorCount()));
        stats.setWarningCount(nullSafe(message.getWarningCount()));
        stats.setClearedCount(nullSafe(message.getClearedCount()));
        stats.setTotalCount(nullSafe(message.getCriticalCount()) + nullSafe(message.getMajorCount())
                + nullSafe(message.getMinorCount()) + nullSafe(message.getWarningCount()));
        stats.setUpdatedTime(System.currentTimeMillis());
        return stats;
    }

    private int nullSafe(Integer val) {
        return val != null ? val : 0;
    }
}
