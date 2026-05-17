package com.workflowbuilder.monitoring.api;

import com.workflowbuilder.monitoring.api.MonitoringDtos.DashboardResponse;
import com.workflowbuilder.monitoring.service.SystemMonitoringService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitoring")
public class SystemMonitoringController {

    private final SystemMonitoringService service;

    public SystemMonitoringController(SystemMonitoringService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard() {
        return service.dashboard();
    }
}
