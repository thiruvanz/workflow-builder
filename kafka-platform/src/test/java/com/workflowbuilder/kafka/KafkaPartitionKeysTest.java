package com.workflowbuilder.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class KafkaPartitionKeysTest {

    @Test
    void forWorkflowId_nullUsesFallback() {
        assertEquals("unkeyed-workflow", KafkaPartitionKeys.forWorkflowId(null));
    }

    @Test
    void forWorkflowId_serializesId() {
        assertEquals("99", KafkaPartitionKeys.forWorkflowId(99L));
    }

    @Test
    void fromExecutionPayload_extractsWorkflowId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String key = KafkaPartitionKeys.fromExecutionPayload("{\"workflowId\":7}", mapper);
        assertEquals("7", key);
    }

    @Test
    void fromExecutionPayload_emptyUsesStableLabel() {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("empty-payload", KafkaPartitionKeys.fromExecutionPayload("  ", mapper));
    }

    @Test
    void fromExecutionPayload_invalidJsonFallsBackToHash() {
        ObjectMapper mapper = new ObjectMapper();
        String key = KafkaPartitionKeys.fromExecutionPayload("not-json", mapper);
        assertTrue(key.startsWith("hash-"));
    }
}
