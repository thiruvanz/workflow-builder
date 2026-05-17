package com.workflowbuilder.execution;

import com.workflowbuilder.resilience.config.ResilienceSupportConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ResilienceSupportConfig.class)
public class WorkflowExecutionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowExecutionServiceApplication.class, args);
    }
}
