# Pharmacy Management System

Enterprise Pharmacy Management System built with Spring Boot 3.4.5 and Java 21.

## Prerequisites

- JDK 21+
- MySQL 8.x running locally
- Database: `pharmacy_db` (auto-created by Hibernate DDL)
- MySQL credentials: `root` / `root` (configure in `application.yml`)

## Quick Start

```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080`.

## Login Credentials

| Username      | Password      | Role      |
|---------------|---------------|-----------|
| `admin`       | `password123` | ADMIN     |
| `eczaci_ayse` | `password123` | PHARMACIST|
| `kasiyer_veli`| `password123` | CASHIER   |

Credentials are reset on every startup via `CommandLineRunner`.

## Tech Stack

- Spring Boot 3.4.5
- Spring Security (form login)
- Spring Data JPA / Hibernate
- MySQL 8.x
- Thymeleaf + Tailwind CSS
- Lombok
- Swagger/OpenAPI (springdoc)

## API Documentation

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Architecture

- Strict 3-tier: Controller → Service → Repository
- DTO pattern (request/response separation)
- Strategy pattern for expiry evaluation
- FIFO inventory costing
- Optimistic locking via @Version
- Soft delete (isActive=false) on all major entities
- Role-based access (ADMIN, PHARMACIST, CASHIER)
