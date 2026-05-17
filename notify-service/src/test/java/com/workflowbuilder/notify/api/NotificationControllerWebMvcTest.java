package com.workflowbuilder.notify.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.workflowbuilder.notify.service.NotificationSink;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationApiController.class)
class NotificationControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    NotificationSink sink;

    @Test
    void getLatestNotifications() throws Exception {
        when(sink.latest()).thenReturn(List.of("hello", "world"));

        mockMvc.perform(get("/api/notify/notifications"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[\"hello\",\"world\"]"));
    }
}
