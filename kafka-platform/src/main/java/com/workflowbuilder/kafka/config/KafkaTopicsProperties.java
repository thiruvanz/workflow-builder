package com.workflowbuilder.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaTopicsProperties(
        @DefaultValue("6") int partitions,
        @DefaultValue("1") int replicationFactor,
        @DefaultValue("workflow.events") String workflowEventsTopic,
        @DefaultValue("execution.events") String executionEventsTopic,
        @DefaultValue("execution.dlq") String executionDlqTopic
) {
}
