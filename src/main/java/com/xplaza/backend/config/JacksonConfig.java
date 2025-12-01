/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Jackson ObjectMapper configuration. Provides a shared, properly configured
 * ObjectMapper bean for the entire application.
 */
@Configuration
public class JacksonConfig {

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();

    // Register Java 8 date/time module
    mapper.registerModule(new JavaTimeModule());

    // Disable writing dates as timestamps (use ISO-8601 instead)
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // Don't fail on unknown properties during deserialization
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    // Don't include null values in JSON output
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    // Pretty print for readability (can be disabled in production if needed)
    mapper.enable(SerializationFeature.INDENT_OUTPUT);

    return mapper;
  }
}
