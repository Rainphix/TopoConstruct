package com.topology.phytopo.kafka;

import cn.hutool.json.JSONUtil;
import com.topology.phytopo.service.SyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * EAM 变更事件消费者
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EamChangeConsumer {

    private final SyncService syncService;

    @KafkaListener(topics = "${kafka.topic.eam-change:eam-changes}", groupId = "topo-service-group")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            String message = record.value();
            log.info("收到EAM变更消息: {}", message);

            EamChangeMessage changeMessage = JSONUtil.toBean(message, EamChangeMessage.class);
            syncService.handleChange(changeMessage);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("处理EAM变更消息失败", e);
            ack.nack(Duration.ofDays(1000L));
        }
    }
}
