[![Main](https://github.com/x-plaza/xplaza-backend/actions/workflows/main.yml/badge.svg?branch=main)](https://github.com/x-plaza/xplaza-backend/actions/workflows/main.yml)

# X-Plaza Backend

Spring Boot backend for X-Plaza multi-vendor e-commerce platform.

## Prerequisites

- Java 24+
- Maven 3.9+

## Quick Start

```bash
# Run locally (H2 database)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Run tests
./mvnw test

# Format code
./mvnw spotless:apply
```

## API

Base URL: `/api/v1/*`

Swagger UI: <http://localhost:8080/swagger-ui/index.html>

### Authentication

All protected endpoints require a JWT token in the Authorization header:

```text
Authorization: Bearer <token>
```

Use `/api/v1/auth/login` to obtain tokens.

## Config

| Profile | Database | Use Case |
|---------|----------|----------|
| `local` | H2 (in-memory) | Development |
| `cloud` | PostgreSQL | Production |

---

© 2025 Xplaza or Xplaza affiliate company. All rights reserved.
