package com.workflowbuilder.notify.api;

import com.workflowbuilder.notify.service.NotificationSink;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notify")
public class NotificationApiController {
    private final NotificationSink sink;

    public NotificationApiController(NotificationSink sink) {
        this.sink = sink;
    }

    @GetMapping("/notifications")
    public List<String> getLatestNotifications() {
        return sink.latest();
    }
}
