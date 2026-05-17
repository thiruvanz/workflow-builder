package com.workflowbuilder.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@AutoConfiguration
@ConditionalOnClass(KafkaAdmin.class)
@EnableConfigurationProperties(KafkaTopicsProperties.class)
@ConditionalOnProperty(prefix = "app.kafka", name = "topics-bootstrap-enabled", havingValue = "true", matchIfMissing = true)
public class KafkaTopicAutoConfiguration {

    @Bean
    public NewTopic workflowEventsTopic(KafkaTopicsProperties p) {
        return TopicBuilder.name(p.workflowEventsTopic())
                .partitions(p.partitions())
                .replicas(p.replicationFactor())
                .build();
    }

    @Bean
    public NewTopic executionEventsTopic(KafkaTopicsProperties p) {
        return TopicBuilder.name(p.executionEventsTopic())
                .partitions(p.partitions())
                .replicas(p.replicationFactor())
                .build();
    }

    @Bean
    public NewTopic executionDlqTopic(KafkaTopicsProperties p) {
        return TopicBuilder.name(p.executionDlqTopic())
                .partitions(p.partitions())
                .replicas(p.replicationFactor())
                .build();
    }
}
