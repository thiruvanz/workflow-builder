package com.workflowbuilder.monitoring.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.workflowbuilder.monitoring.api.MonitoringDtos.DashboardResponse;
import com.workflowbuilder.monitoring.service.SystemMonitoringService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SystemMonitoringController.class)
class MonitoringControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    SystemMonitoringService service;

    @Test
    void getDashboardReturnsJson() throws Exception {
        DashboardResponse body =
                new DashboardResponse(3, 2, 10, Map.of("COMPLETED", 5L), Map.of(), List.of(), List.of());
        when(service.dashboard()).thenReturn(body);

        mockMvc.perform(get("/api/monitoring/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalWorkflows").value(3))
                .andExpect(jsonPath("$.totalRuns").value(10));
    }
}
