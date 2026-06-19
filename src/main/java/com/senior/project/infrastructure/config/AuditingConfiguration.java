package com.senior.project.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Configuration for data auditing and other infrastructure concerns.
 * 
 * Demonstrates:
 * - Spring configuration best practices
 * - AuditorAware for automatic audit tracking
 * - Security context integration
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfiguration {

    /**
     * Provides the current user for audit fields
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    return Optional.of(authentication.getName());
                }
            } catch (Exception e) {
                // Log but don't fail
            }
            return Optional.of("system");
        };
    }
}
