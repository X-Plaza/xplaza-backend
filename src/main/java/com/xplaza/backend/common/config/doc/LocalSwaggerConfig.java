/*
 * Copyright (c) 2024 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.common.config.doc;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class LocalSwaggerConfig {
  @Bean
  public GroupedOpenApi publicApis() {
    return GroupedOpenApi.builder()
        .group("Public APIs")
        .pathsToMatch("/api/public/**")
        .build();
  }

  @Bean
  public GroupedOpenApi internalApis() {
    return GroupedOpenApi.builder()
        .group("Internal APIs")
        .pathsToExclude("/api/public/**")
        .build();
  }

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI().info(new Info().title("Backend for X-Plaza Platform.")
        .description("This API documentation enables you to perform operations on X-Plaza platform.")
        .version("v1.0.0"));
  }
}
