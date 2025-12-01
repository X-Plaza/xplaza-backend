[![Main](https://github.com/x-plaza/xplaza-backend/actions/workflows/main.yml/badge.svg?branch=main)](https://github.com/x-plaza/xplaza-backend/actions/workflows/main.yml)

# X-Plaza Backend Service

This repository contains the `backend` service for the X-Plaza Platform. The X-Plaza backend service is a [Spring Boot](https://spring.io/projects/spring-boot) Application.

## Tech Stack

- **Java 24** with Spring Boot 3.5.7
- **Spring Security** with JWT authentication
- **Spring Data JPA** with H2 (local) / PostgreSQL (production)
- **MapStruct** for object mapping
- **Flyway** for database migrations
- **Spotless** for code formatting
- **JaCoCo** for code coverage

## Architecture

The application follows a layered architecture:

```
Controller → Service → Repository → DAO (Database Entity)
     ↓           ↓
    DTO ←→ Entity (via MapStruct mappers)
```

- **Controllers** (`http/controller/`) - REST API endpoints, handle HTTP requests
- **Services** (`service/`) - Business logic layer
- **Repositories** (`jpa/repository/`) - Data access layer using Spring Data JPA
- **DAOs** (`jpa/dao/`) - JPA entities mapped to database tables
- **DTOs** (`http/dto/`) - Data transfer objects for API requests/responses
- **Entities** (`service/entity/`) - Domain objects used in service layer
- **Mappers** (`mapper/`) - MapStruct mappers for object conversion

## Local Development Setup

### Prerequisites

- JDK 24 or later
- Maven 3.9+
- Docker (optional, for PostgreSQL)

### Build and Test

To build the application and run all tests:

```bash
mvn clean package
```

### Running the Application

#### Local Profile (H2 Database)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

This runs with an embedded [H2 database](https://www.h2database.com/html/main.html). The H2 console is available at `/h2-console`.

#### Cloud Profile (PostgreSQL)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=cloud
```

Requires PostgreSQL connection configured in `application-cloud.properties`.

### Code Formatting

Format source code using Spotless:

```bash
mvn spotless:apply
```

Check formatting without applying:

```bash
mvn spotless:check
```

### Running Tests

Run all tests:

```bash
mvn test
```

Run tests with coverage report:

```bash
mvn test jacoco:report
```

Coverage report will be available at `target/site/jacoco/index.html`.

## API Documentation

API documentation is available on Swagger UI:
- **Production**: [https://api.xplaza.shop/swagger-ui/index.html](https://api.xplaza.shop/swagger-ui/index.html)
- **Local**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### API Versions

| Version | Status | Base Path | Notes |
|---------|--------|-----------|-------|
| V1 | ⚠️ Deprecated | `/api/v1/*` | Legacy endpoints, will be removed |
| V2 | ✅ Current | `/api/v2/*` | Clean REST API with pagination |

> **Note:** V1 endpoints are deprecated and marked for removal. Please migrate to V2 API.

## Project Documentation

Additional documentation is available in the [`docs/`](docs/) folder:

- [API Refactoring Strategy](docs/API_REFACTORING_STRATEGY.md) - V2 API design and migration guide
- [Code Review Findings](docs/CODE_REVIEW_FINDINGS.md) - Initial code review and issues found
- [Refactoring Todo](docs/REFACTORING_TODO.md) - Task list and progress tracking

## Configuration

Key configuration files:

- `src/main/resources/application.properties` - Base configuration
- `src/main/resources/application-local.properties` - Local development settings
- `src/main/resources/application-cloud.properties` - Production settings

### Environment Variables (Production)

| Variable | Description |
|----------|-------------|
| `DB_HOST` | PostgreSQL host |
| `DB_PORT` | PostgreSQL port |
| `DB_NAME` | Database name |
| `DB_USER` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | JWT signing secret (min 64 chars) |
| `MAIL_USERNAME` | SMTP username |
| `MAIL_PASSWORD` | SMTP password |

## Docker

Build Docker image:

```bash
docker build -t xplaza-backend .
```

Run container:

```bash
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=cloud xplaza-backend
```

## License

This project is proprietary software. See LICENSE file for details.
