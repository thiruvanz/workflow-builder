package com.workflowbuilder.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers("/", "/js/**", "/actuator/**").permitAll()
                        .pathMatchers("/api/**", "/bff/**").authenticated()
                        .anyExchange().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    ReactiveJwtDecoder jwtDecoder() {
        byte[] secret = "workflow-platform-demo-secret-workflow-platform".getBytes();
        SecretKeySpec key = new SecretKeySpec(secret, "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(key).build();
    }
}
