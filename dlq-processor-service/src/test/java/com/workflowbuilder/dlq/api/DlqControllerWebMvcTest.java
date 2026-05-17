package com.workflowbuilder.dlq.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.workflowbuilder.dlq.service.DeadLetterQueueService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DeadLetterQueueController.class)
class DlqControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    DeadLetterQueueService service;

    @Test
    void getFailedMessages() throws Exception {
        when(service.list()).thenReturn(List.of("a", "b"));

        mockMvc.perform(get("/api/dlq/failed-messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("a"));
    }

    @Test
    void replayFailedMessages() throws Exception {
        when(service.replayAll()).thenReturn(4);

        mockMvc.perform(post("/api/dlq/replay-failed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.replayed").value(4));
    }
}
