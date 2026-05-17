package com.workflowbuilder.notify.domain;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface NotificationRepository extends ElasticsearchRepository<NotificationDocument, String> {
}
