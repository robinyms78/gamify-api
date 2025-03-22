# Gamify Demo - Technology Stack

## Core Framework
- **Spring Boot 3.2.5**  
  Foundation for building production-grade Java application with embedded Tomcat server

## Database & Persistence
- **PostgreSQL**  
  Primary relational database for production
- **H2 Database**  
  In-memory database for testing (`@ActiveProfiles("test")`)
- **Spring Data JPA**  
  Simplified database access through repositories
- **Hibernate 6.3**  
  ORM implementation with JSON column support via `@Type(JsonType.class)`

## Security
- **Spring Security**  
  Authentication and authorization framework
- **JSON Web Tokens (JWT)**  
  Token-based authentication using `jjwt` 0.11.5

## API Development
- **Spring Web MVC**  
  REST API endpoints using `@RestController`
- **SpringDoc OpenAPI 2.5.0**  
  API documentation with Swagger UI
- **Jakarta Validation**  
  Request validation through `@Valid` annotations

## JSON Handling
- **Jackson 2.15**  
  JSON serialization/deserialization
- **Hypersistence Utils 3.7.3**  
  Advanced Hibernate JSON support for `JsonNode` storage

## Testing
- **JUnit 5**  
  Test framework with Spring Boot integration
- **Spring Boot Test**  
  Integration test support
- **Testcontainers**  
  Implied database testing infrastructure

## Code Quality & Productivity
- **Lombok 1.18.32**  
  Boilerplate reduction through `@Getter/@Builder` etc.
- **MapStruct 1.5.5**  
  Type-safe DTO mapping
- **Spring Boot DevTools**  
  Developer productivity enhancements

## Monitoring & Metrics
- **Spring Boot Actuator**  
  Production monitoring endpoints
- **Micrometer**  
  Application metrics collection

## Build & Deployment
- **Maven**  
  Dependency management and build automation
- **Java 17**  
  Language version
- **Spring Boot Maven Plugin**  
  Executable JAR/WAR packaging

## Key Architectural Features
- **JSON-Backed Criteria Storage**  
  Flexible achievement criteria using PostgreSQL JSON columns
- **UUID Primary Keys**  
  String-based UUIDs for entity identifiers
- **Layered Architecture**  
  Clear separation between Controllers ↔ Services ↔ Repositories
- **Domain-Driven Design**  
  Rich domain models with business logic encapsulation
- **Integration Testing**  
  Comprehensive repository-level testing with test profile

## Infrastructure
- **JVM Memory Management**  
  Optimized through Spring Boot defaults
- **Transactional Boundaries**  
  ACID compliance via `@Transactional` annotations
- **Unique Constraints**  
  Database-level enforcement for critical fields (email, achievement names)

This stack was chosen for its:
- Production readiness
- Type safety
- JSON flexibility
- Testing capabilities
- Modern Java ecosystem integration
