package com.workflowbuilder.trigger.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflowbuilder.trigger.security.WebhookSecretValidator;
import com.workflowbuilder.trigger.service.WebhookDispatcherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TriggerController.class)
@Import(WebhookSecretValidator.class)
@TestPropertySource(properties = "trigger.webhook.secret=test-webhook-secret")
class TriggerControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    WebhookDispatcherService dispatcherService;

    @Test
    void webhookReturns401WhenSecretMissing() throws Exception {
        mockMvc.perform(
                        post("/api/triggers/webhook")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new TriggerRequest(1L, "t", "k"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void webhookReturns401WhenSecretWrong() throws Exception {
        mockMvc.perform(
                        post("/api/triggers/webhook")
                                .header("X-Webhook-Secret", "wrong")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new TriggerRequest(1L, "t", "k"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void webhookAcceptsValidSecret() throws Exception {
        mockMvc.perform(
                        post("/api/triggers/webhook")
                                .header("X-Webhook-Secret", "test-webhook-secret")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new TriggerRequest(1L, "t", "k"))))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Webhook trigger accepted"));

        verify(dispatcherService).dispatch(any(TriggerRequest.class));
    }
}
