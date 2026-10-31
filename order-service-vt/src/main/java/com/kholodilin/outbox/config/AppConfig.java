package com.kholodilin.outbox.config;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/** Spring beans shared by the order service (JSON mapper, Kafka producer template). */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {

    /**
     * Shared Jackson 3 {@link ObjectMapper} for HTTP bodies and outbox payloads.
     * <p>
     * Declared as {@link JsonMapper} so Spring Boot's {@code jacksonJsonMapper} backs off
     * and the outbox starter reuses this mapper.
     */
    @Bean
    @Primary
    public JsonMapper objectMapper() {
        return JsonMapper.builder().build();
    }
}
