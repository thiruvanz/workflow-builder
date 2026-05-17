package com.workflowbuilder.bff;

import com.workflowbuilder.resilience.config.ResilienceSupportConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ResilienceSupportConfig.class)
public class BffServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BffServiceApplication.class, args);
    }
}
