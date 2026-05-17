package com.workflowbuilder.trigger.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.workflowbuilder.trigger.security.WebhookSecretValidator;
import com.workflowbuilder.trigger.service.WebhookDispatcherService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

class TriggerControllerTest {

    @Test
    void webhookAcceptsTriggerRequest() {
        WebhookDispatcherService service = Mockito.mock(WebhookDispatcherService.class);
        TriggerController controller = new TriggerController(service, new WebhookSecretValidator(""));

        var response = controller.webhook(null, new TriggerRequest(1L, "tenant", "key"));

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("Webhook trigger accepted", response.getBody());
    }
}
