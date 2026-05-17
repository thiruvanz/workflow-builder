package com.workflowbuilder.monitoring.service;

import com.workflowbuilder.monitoring.domain.EventMetricDocument;
import com.workflowbuilder.monitoring.domain.EventMetricRepository;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EventMetricsStore {
    private final Map<String, Long> eventCounters = new ConcurrentHashMap<>();
    private final EventMetricRepository repository;

    public EventMetricsStore(EventMetricRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(
            topics = {
                    "${app.kafka.workflow-events-topic:workflow.events}",
                    "${app.kafka.execution-events-topic:execution.events}"
            },
            groupId = "monitoring-service"
    )
    public void onEvent(String payload) {
        String key = payload.contains("COMPLETED") ? "COMPLETED" : "OTHER";
        eventCounters.merge(key, 1L, Long::sum);
        repository.save(new EventMetricDocument(payload, Instant.now()));
    }

    public Map<String, Long> snapshot() {
        return Map.copyOf(eventCounters);
    }
}
