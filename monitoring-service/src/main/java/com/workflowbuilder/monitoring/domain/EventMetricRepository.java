package com.workflowbuilder.monitoring.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventMetricRepository extends MongoRepository<EventMetricDocument, String> {
}
