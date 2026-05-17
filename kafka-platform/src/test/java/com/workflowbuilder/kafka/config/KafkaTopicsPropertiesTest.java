package com.workflowbuilder.kafka.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class KafkaTopicsPropertiesTest {

    @Test
    void recordHoldsTopicConfiguration() {
        KafkaTopicsProperties props =
                new KafkaTopicsProperties(6, 1, "workflow.events", "execution.events", "execution.dlq");

        assertEquals(6, props.partitions());
        assertEquals(1, props.replicationFactor());
        assertEquals("workflow.events", props.workflowEventsTopic());
        assertEquals("execution.events", props.executionEventsTopic());
        assertEquals("execution.dlq", props.executionDlqTopic());
    }
}
