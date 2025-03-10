# Gamify Demo Application: Admin/Developer Guide

This guide provides information for administrators and developers on configuring and customizing the Gamify Demo application.

## Introduction

[Briefly describe the purpose of this guide.]

## System Architecture

[High-level overview of the application's architecture. Include a diagram if possible.]

*   **Components:** [List major components, e.g., frontend (JavaScript, likely a framework), backend (Spring Boot), database (PostgreSQL).]
*   **Technologies:** [List key technologies used: Spring Boot, PostgreSQL, JavaScript (potentially a framework like React, Angular, or Vue.js - `main.js` suggests vanilla JS, but a framework is possible), JWT for authentication.]
*   **Data Flow:** [Describe how data flows between components.  e.g., User interacts with frontend -> Frontend sends requests to backend API -> Backend processes requests, interacts with database, and sends responses back to frontend.]

## Configuration

### Environment Variables

The application uses environment variables for sensitive configuration values. These should be set in your deployment environment (e.g., Heroku, AWS, Docker).

*   **`SPRING_DATASOURCE_URL`:** The JDBC URL for the PostgreSQL database.  Example: `jdbc:postgresql://<host>:<port>/<database>?sslmode=require`
*   **`SPRING_DATASOURCE_USERNAME`:** The username for the PostgreSQL database.
*   **`SPRING_DATASOURCE_PASSWORD`:** The password for the PostgreSQL database.
*   **`JWT_SECRET`:** A secret key used for signing JWT tokens.  This should be a long, random, and highly secure string.
*   **`JWT_EXPIRATION_MS`:** The expiration time for JWT tokens, in milliseconds.

### Application Properties

The `application.properties` file contains various configuration settings. Here's a breakdown of the key ones:

*   **Database Configuration:**
    *   `spring.datasource.driver-class-name=org.postgresql.Driver`: Specifies the PostgreSQL JDBC driver.
    *   `spring.datasource.hikari.ssl-mode=require`: Enforces SSL connections to the database.

*   **JPA/Hibernate Configuration:**
    *   `spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect`: Specifies the Hibernate dialect for PostgreSQL.
    *   `spring.jpa.hibernate.ddl-auto=update`:  This setting tells Hibernate to automatically update the database schema to match the entity definitions.  **Important:** In a production environment, you might want to use `validate` or manage schema changes with a migration tool like Flyway or Liquibase instead of `update`.
    *   `spring.jpa.show-sql=true`:  Logs SQL queries to the console.  Useful for debugging, but should generally be disabled in production.
    *   `spring.jpa.properties.hibernate.format_sql=true`: Formats the logged SQL queries for better readability.

*   **Error Handling Configuration:**
    *   `server.error.whitelabel.enabled=false`: Disables the default Spring Boot "Whitelabel Error Page."
    *   `server.error.include-stacktrace=never`, `server.error.include-message=never`, `server.error.include-binding-errors=never`, `server.error.include-exception=false`: These settings control how much error information is included in error responses.  For security reasons, it's best to minimize the information exposed in production.

*   **Static Resources Configuration:**
    *   `spring.web.resources.static-locations=classpath:/static/`: Specifies the location of static resources (like JavaScript files, CSS, images).

*   **JWT Configuration:**
    *   `jwt.secret=${JWT_SECRET}`: References the `JWT_SECRET` environment variable.
    *   `jwt.expiration.ms=${JWT_EXPIRATION_MS}`: References the `JWT_EXPIRATION_MS` environment variable.

### 1. Achievement Management

[How to create, update, and delete achievement types.]

*   **Data Model:** [**ASSUMPTION:** There's an `Achievement` entity (likely in `models` package) and possibly an `AchievementType` entity.  We need to see these files to provide accurate information.]
*   **Database Table:** [**ASSUMPTION:** Achievements are stored in a table, likely named `achievements` or `achievement_types`. We need to see the entity definitions to confirm.]
*   **Admin Interface:** [**ASSUMPTION:** There's likely a web-based admin interface for managing achievements. `main.js` includes code for handling form submissions and button clicks, suggesting dynamic interactions, possibly with an admin section.]
*   **API Endpoints:** [**ASSUMPTION:** There are likely REST API endpoints for managing achievements, probably under `/api/admin/achievements`.  We need to see the controller classes to confirm.]
    *   **Create Achievement Type:** (e.g., `POST /api/admin/achievements/types`) - **Hypothetical**
    *   **Update Achievement Type:** (e.g., `PUT /api/admin/achievements/types/{id}`) - **Hypothetical**
    *   **Delete Achievement Type:** (e.g., `DELETE /api/admin/achievements/types/{id}`) - **Hypothetical**

**Hypothetical Achievement Type Attributes:** (These are *guesses* until we see the actual code)

*   `name` (String): Unique identifier.
*   `description` (String): User-friendly description.
*   `criteriaType` (String/Enum):  e.g., "TASK_COMPLETION", "POINTS_EARNED".
*   `criteriaValue` (Number/String): The value for the criteria.
*   `icon` (String):  Path or URL to an icon.

### 2. Ladder Level Management

[How to configure ladder level thresholds.]

*   **Data Model:** [**ASSUMPTION:** There's a `LadderLevel` entity (likely in `models` package). We need to see this file.]
*   **Database Table:** [**ASSUMPTION:** Ladder levels are stored in a table, likely named `ladder_levels`. We need to see the entity definition.]
*   **Admin Interface:** [**ASSUMPTION:** There's likely a web-based admin interface for managing ladder levels.]
*   **API Endpoints:** [**ASSUMPTION:** There are likely REST API endpoints for managing ladder levels, probably under `/api/admin/ladder`. We need to see the controller classes.]
    *   **Update Ladder Level:** (e.g., `PUT /api/admin/ladder/levels/{level}`) - **Hypothetical**

**Hypothetical Ladder Level Attributes:** (These are *guesses* until we see the actual code)

*   `level` (Number): The level number.
*   `pointsRequired` (Number): Points needed to reach this level.

### 3. Points Calculation

[How to configure points calculation rules.]

*   **Strategy Pattern:** [**ASSUMPTION:** The application uses the Strategy pattern with a `TaskPointsCalculationStrategy` interface and implementations like `PriorityBasedPointsStrategy`. We *must* see these files to provide accurate documentation.]
*   **Available Strategies:** [**ASSUMPTION:** There's at least a `PriorityBasedPointsStrategy`. We need to see the code to list and describe all available strategies.]
*   **Configuration:** [**ASSUMPTION:** The mapping between task types/properties and point calculation strategies, and the parameters for those strategies, are configured either in a configuration file (like `application.yml` - *not* `application.properties`, which is for basic settings) or in a database table. We need to see the relevant code and configuration files.]
    *   **Configuration File:** (e.g., `application.yml` or a custom file) - **Possible, but needs confirmation.**
    *   **Database Table:** (If configuration is stored in the database) - **Possible, but needs confirmation.**
* **Code Modification (Likely):** Changing parameters *within* a strategy (e.g. the points awarded for each priority in `PriorityBasedPointsStrategy`) likely requires code modification.

## Deployment

[Instructions for deploying the application.]

*   **Prerequisites:**
    *   Java Development Kit (JDK) 17 or later (likely, given Spring Boot).
    *   PostgreSQL database.
    *   Maven (likely, for building the project).
    *   Environment variables set (see above).
*   **Steps:**
    1.  **Clone the repository:** `git clone <repository_url>`
    2.  **Build the project:** `mvn clean install` (This assumes Maven is used.)
    3.  **Run the application:** `java -jar target/gamify-demo.jar` (The exact JAR file name might vary.)
    4.  **Access the application:** Open a web browser and go to `http://localhost:8080` (or the configured port).

## Code Style and Conventions

[Guidelines for contributing code.]

* **Use Consistent Formatting**
* **Write Clear and Concise Code**
* **Add Javadoc Comments**

## Testing

[Information about testing the application.]
* **Running Tests:** `mvn test`

---
