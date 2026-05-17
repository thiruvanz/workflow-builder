package com.workflowbuilder.trigger.api;

import com.workflowbuilder.trigger.security.WebhookSecretValidator;
import com.workflowbuilder.trigger.service.WebhookDispatcherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/triggers")
public class TriggerApiController {
    private final WebhookDispatcherService dispatcherService;
    private final WebhookSecretValidator webhookSecretValidator;

    public TriggerApiController(
            WebhookDispatcherService dispatcherService,
            WebhookSecretValidator webhookSecretValidator) {
        this.dispatcherService = dispatcherService;
        this.webhookSecretValidator = webhookSecretValidator;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String webhookSecret,
            @RequestBody TriggerRequest request) {
        webhookSecretValidator.validate(webhookSecret);
        dispatcherService.dispatch(request);
        return ResponseEntity.accepted().body("Webhook trigger accepted");
    }
}
