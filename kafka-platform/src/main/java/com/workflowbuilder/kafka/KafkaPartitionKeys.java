package com.workflowbuilder.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Consistent Kafka record keys so related events share a partition (ordering per workflow)
 * and load spreads across partitions when keys differ.
 */
public final class KafkaPartitionKeys {

    private KafkaPartitionKeys() {
    }

    public static String forWorkflowId(Long workflowId) {
        return workflowId == null ? "unkeyed-workflow" : Long.toString(workflowId);
    }

    /**
     * Prefer workflow id from JSON payloads (execution / DLQ); fall back to a stable hash of the body.
     */
    public static String fromExecutionPayload(String json, ObjectMapper objectMapper) {
        if (json == null || json.isBlank()) {
            return "empty-payload";
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.hasNonNull("workflowId")) {
                return root.get("workflowId").asText();
            }
        } catch (Exception ignored) {
            // fall through
        }
        return "hash-" + (json.hashCode() & 0x7fffffff);
    }
}
