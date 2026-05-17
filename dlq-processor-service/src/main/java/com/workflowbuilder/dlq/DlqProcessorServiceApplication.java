package com.workflowbuilder.dlq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class DlqProcessorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DlqProcessorServiceApplication.class, args);
    }
}
