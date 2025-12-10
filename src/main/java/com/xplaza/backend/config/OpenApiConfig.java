/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for API documentation.
 */
@Configuration
public class OpenApiConfig {

  private static final String SECURITY_SCHEME_NAME = "bearerAuth";

  /**
   * Main OpenAPI configuration with security and general info. Server URL is
   * automatically inferred from the request URL by SpringDoc.
   */
  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("X-Plaza Backend API")
            .description("""
                X-Plaza Multi-Vendor E-Commerce Platform API Documentation.

                ## Authentication
                All protected endpoints require a valid JWT token in the Authorization header.
                Use the `/api/v1/auth/login` endpoint to obtain tokens.
                """)
            .version("1.0.0")
            .contact(new Contact()
                .name("X-Plaza Team")
                .url("https://github.com/x-plaza")
                .email("support@xplaza.com"))
            .license(new License()
                .name("Proprietary")
                .url("https://xplaza.com/terms")))
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
        .components(new Components()
            .addSecuritySchemes(SECURITY_SCHEME_NAME,
                new SecurityScheme()
                    .name(SECURITY_SCHEME_NAME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Enter your JWT token. Click 'Authorize' to add your token.")));
  }
}
