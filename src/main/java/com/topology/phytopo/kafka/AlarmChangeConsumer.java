package com.topology.phytopo.kafka;

import cn.hutool.json.JSONUtil;
import com.topology.phytopo.service.AlarmStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 告警变更事件消费者
 * <p>
 * 消费告警变更消息并通过 AlarmStatsService 更新告警统计。
 * 支持增量更新（ALARM_NEW/ALARM_CLEARED）和全量快照（ALARM_STATS_SNAPSHOT）两种模式，
 * 均保证多实例部署下的数据一致性。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlarmChangeConsumer {

    private final AlarmStatsService alarmStatsService;

    @KafkaListener(topics = "${kafka.topic.alarm-change:alarm-changes}", groupId = "topo-service-group")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            String message = record.value();
            log.info("收到告警变更消息: {}", message);

            AlarmChangeMessage changeMessage = JSONUtil.toBean(message, AlarmChangeMessage.class);
            alarmStatsService.handleAlarmChange(changeMessage);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("处理告警变更消息失败", e);
            ack.nack(Duration.ofMillis(1000));
        }
    }
}
