package com.workflowbuilder.monitoring;

import com.workflowbuilder.resilience.config.ResilienceSupportConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
@Import(ResilienceSupportConfig.class)
public class SystemMonitoringServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemMonitoringServiceApplication.class, args);
    }
}
