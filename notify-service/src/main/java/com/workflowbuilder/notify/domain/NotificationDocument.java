package com.workflowbuilder.notify.domain;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "notifications")
public class NotificationDocument {
    @Id
    private String id;
    private String payload;
    private Instant createdAt;

    public NotificationDocument() {}

    public NotificationDocument(String id, String payload, Instant createdAt) {
        this.id = id;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getPayload() { return payload; }
    public Instant getCreatedAt() { return createdAt; }
}
