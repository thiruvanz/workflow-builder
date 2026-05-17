package com.workflowbuilder.dlq.api;

import com.workflowbuilder.dlq.service.DeadLetterQueueService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dlq")
public class DeadLetterQueueController {
    private final DeadLetterQueueService service;

    public DeadLetterQueueController(DeadLetterQueueService service) {
        this.service = service;
    }

    @GetMapping("/failed-messages")
    public List<String> getFailedMessages() {
        return service.list();
    }

    @PostMapping("/replay-failed")
    public Map<String, Object> replayFailedMessages() {
        return Map.of("replayed", service.replayAll());
    }
}
