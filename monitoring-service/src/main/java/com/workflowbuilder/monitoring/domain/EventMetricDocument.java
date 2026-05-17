package com.workflowbuilder.monitoring.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("event_metrics")
public class EventMetricDocument {
    @Id
    private String id;
    private String payload;
    private Instant createdAt;

    public EventMetricDocument() {}

    public EventMetricDocument(String payload, Instant createdAt) {
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getPayload() { return payload; }
    public Instant getCreatedAt() { return createdAt; }
}
