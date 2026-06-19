package com.senior.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main entry point for the Product Service application.
 * 
 * Demonstrates senior-level Spring Boot configuration with:
 * - Proper component scanning
 * - JPA auditing for entity lifecycle management
 * - Clean architecture separation
 */
@SpringBootApplication
@EnableJpaAuditing
@ComponentScan(basePackages = {"com.senior.project"})
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
