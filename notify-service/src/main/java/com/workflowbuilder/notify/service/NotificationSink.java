package com.workflowbuilder.notify.service;

import com.workflowbuilder.notify.domain.NotificationDocument;
import com.workflowbuilder.notify.domain.NotificationRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationSink {
    private final List<String> messages = new ArrayList<>();
    private final NotificationRepository repository;

    public NotificationSink(NotificationRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(
            topics = {
                    "${app.kafka.execution-events-topic:execution.events}",
                    "${app.kafka.workflow-events-topic:workflow.events}"
            },
            groupId = "notify-service"
    )
    public void consume(String payload) {
        synchronized (messages) {
            messages.add(payload);
            if (messages.size() > 1000) {
                messages.remove(0);
            }
        }
        repository.save(new NotificationDocument(UUID.randomUUID().toString(), payload, Instant.now()));
    }

    public List<String> latest() {
        synchronized (messages) {
            return List.copyOf(messages);
        }
    }
}
