package com.workflowbuilder.dlq.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflowbuilder.kafka.KafkaPartitionKeys;
import com.workflowbuilder.kafka.config.KafkaTopicsProperties;
import java.util.ArrayList;
import java.util.List;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DeadLetterQueueService {
    private final List<String> messages = new ArrayList<>();
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaTopicsProperties topicNames;

    public DeadLetterQueueService(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            KafkaTopicsProperties topicNames
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topicNames = topicNames;
    }

    @KafkaListener(topics = "${app.kafka.execution-dlq-topic:execution.dlq}", groupId = "dlq-processor")
    public void consume(String payload) {
        synchronized (messages) {
            messages.add(payload);
            if (messages.size() > 500) messages.remove(0);
        }
    }

    public List<String> list() {
        synchronized (messages) {
            return List.copyOf(messages);
        }
    }

    public int replayAll() {
        List<String> snapshot = list();
        snapshot.forEach(msg -> kafkaTemplate.send(
                topicNames.executionEventsTopic(),
                KafkaPartitionKeys.fromExecutionPayload(msg, objectMapper),
                msg));
        return snapshot.size();
    }
}
