package com.ibm.cmod.ondemand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application for On-Demand Statements Service
 * Integrates with ODWEK and IBM CMOD for AFP file management
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class OnDemandStatementsApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnDemandStatementsApplication.class, args);
    }
}
