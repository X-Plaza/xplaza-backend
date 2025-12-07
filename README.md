# X-Plaza Backend

[![Build Status](https://github.com/x-plaza/xplaza-backend/actions/workflows/main.yml/badge.svg?branch=main)](https://github.com/x-plaza/xplaza-backend/actions/workflows/main.yml)
![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-green)

Backend service for the X-Plaza e-commerce platform.

## Architecture

The application is structured as a **Modular Monolith** following **Domain-Driven Design (DDD)** principles. Code is organized by business domain rather than technical layer to enforce boundaries and simplify maintenance.

### Domains

- **`catalog`**: Product management, inventory, and categorization.
- **`order`**: Order processing, state management, and checkout flows.
- **`auth`**: Authentication (JWT) and authorization.
- **`cart`**: Shopping session management.

## Technology Stack

- **Java 25** (SAP Machine)
- **Spring Boot 4.0.0**
- **PostgreSQL 17** (Production) / **H2** (Development)
- **Hibernate 7** / Spring Data JPA
- **Flyway** for database migrations

## Development Setup

### Prerequisites

- JDK 25
- Docker (optional, for local PostgreSQL)

### Build & Run

#### 1. Build

```bash
./mvnw clean install -DskipTests
```

#### 2. Run (Local Profile)

Uses in-memory H2 database.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

#### 3. Run (Cloud Profile)

Requires a running PostgreSQL instance.

```bash
export DB_URL=jdbc:postgresql://localhost:5432/xplaza
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
./mvnw spring-boot:run -Dspring-boot.run.profiles=cloud
```

## Testing & Code Quality

The project uses **Spotless** to enforce Google Java Style and **JaCoCo** for coverage.

```bash
# Run tests
./mvnw test

# Apply code formatting
./mvnw spotless:apply
```

## Deployment

The application is containerized using Docker.

```bash
docker build -t xplaza-backend .
```

### Profiles

| Profile | Database | Migrations | Usage |
|---------|----------|------------|-------|
| `local` | H2 | Disabled | Local development |
| `cloud` | PostgreSQL | Enabled | Production / CI |
