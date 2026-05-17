package com.workflowbuilder.monitoring.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.workflowbuilder.monitoring.api.MonitoringDtos.DashboardResponse;
import com.workflowbuilder.monitoring.service.SystemMonitoringService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MonitoringControllerTest {

    @Test
    void dashboardReturnsServicePayload() {
        SystemMonitoringService service = Mockito.mock(SystemMonitoringService.class);
        MonitoringController controller = new MonitoringController(service);
        DashboardResponse expected = new DashboardResponse(1, 1, 1, Map.of("COMPLETED", 1L), Map.of(), List.of(), List.of());

        when(service.dashboard()).thenReturn(expected);

        DashboardResponse actual = controller.dashboard();
        assertEquals(expected, actual);
    }
}
