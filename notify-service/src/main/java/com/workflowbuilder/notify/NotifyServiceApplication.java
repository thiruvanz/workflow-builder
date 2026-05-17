package com.workflowbuilder.notify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class NotifyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotifyServiceApplication.class, args);
    }
}
