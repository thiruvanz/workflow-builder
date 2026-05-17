package com.workflowbuilder.trigger;

import com.workflowbuilder.resilience.config.ResilienceSupportConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Import(ResilienceSupportConfig.class)
public class TriggerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TriggerServiceApplication.class, args);
    }
}
