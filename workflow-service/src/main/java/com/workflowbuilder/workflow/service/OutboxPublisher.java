package com.workflowbuilder.workflow.service;

import com.workflowbuilder.kafka.KafkaPartitionKeys;
import com.workflowbuilder.kafka.config.KafkaTopicsProperties;
import com.workflowbuilder.workflow.domain.OutboxEvent;
import com.workflowbuilder.workflow.domain.OutboxEventRepository;
import java.util.List;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxPublisher {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicsProperties topicNames;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            KafkaTopicsProperties topicNames
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.topicNames = topicNames;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outboxEventRepository.findTop50ByStatusOrderByIdAsc("PENDING");
        for (OutboxEvent event : pending) {
            kafkaTemplate.send(
                    topicNames.workflowEventsTopic(),
                    KafkaPartitionKeys.forWorkflowId(event.getAggregateId()),
                    event.getPayload());
            event.setStatus("PUBLISHED");
            outboxEventRepository.save(event);
        }
    }
}
