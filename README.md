# Gamify Demo - Gamification Platform Backend

[![Java Version](https://img.shields.io/badge/Java-17-blue.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.1-green.svg)](https://spring.io/projects/spring-boot)

A robust gamification backend service implementing core game mechanics for user engagement systems.

## 🎮 Key Features

### Core Game Mechanics
- **User Progression System**
  - Points earning through task completion with priority-based calculations
  - Multi-level ladder system with defined points requirements
  - Achievement system with JSON-based criteria
  - Real-time event processing for progression updates

### Event-Driven Architecture
- Domain events for system reactions
  - `PointsEarnedEvent` with detailed metadata
  - Event processing through `/api/achievements/process/{userId}` endpoint
  - Priority-based points calculation strategy

### Enterprise Features
- REST API endpoints
- JPA/Hibernate persistence
- JSON serialization/deserialization
- Comprehensive validation
- Error handling framework

## 🛠️ Tech Stack

| Component               | Technology                          |
|-------------------------|-------------------------------------|
| **Backend Framework**   | Spring Boot 3.1                     |
| **Database**            | PostgreSQL                         |
| **Testing**             | JUnit 5, Mockito                    |
| **Build Tool**          | Maven                               |
| **Code Quality**        | Lombok, Validation API              |
| **API Documentation**   | OpenAPI (SpringDoc)                 |

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- (Optional) IDE with Spring support

### Installation
```bash
git clone https://github.com/your-org/gamify-demo.git
cd gamify-demo
mvn clean install
```

### Running
```bash
mvn spring-boot:run
```

## 📚 API Documentation

### User Management
```http
POST /api/users
Content-Type: application/json

{
  "username": "new_user",
  "email": "user@example.com",
  "department": "Engineering"
}
```

### Achievement Management
```http
GET /api/achievements
GET /api/achievements/{achievementId}
POST /api/achievements
PUT /api/achievements/{achievementId} 
DELETE /api/achievements/{achievementId}
GET /api/achievements/user/{userId}
GET /api/achievements/{achievementId}/check/{userId}
POST /api/achievements/process/{userId}
```

### Event Processing
```http
POST /api/achievements/process/{userId}
Content-Type: application/json

{
  "eventType": "TASK_COMPLETED",
  "eventDetails": {
    "taskId": "task-789",
    "priority": "HIGH"  // LOW, MEDIUM, HIGH, CRITICAL
  }
}
```

## 🧪 Testing Strategy

### Test Pyramid Implementation
```bash
mvn test
```

### Key Test Components
- **Domain Event Tests**  
  (See [`DomainEventPublisherTest.java`](src/test/java/sg/edu/ntu/gamify_demo/events/domain/DomainEventPublisherTest.java))
  - Event type validation
  - Data integrity checks
  - Legacy system compatibility
  - Metadata persistence

### Test Coverage
| Aspect                | Coverage Mechanism                  |
|-----------------------|--------------------------------------|
| Business Logic        | Unit tests with Mockito              |
| API Contracts         | Integration tests                    |
| Data Serialization    | JSON assertion validations           |
| Error Conditions      | Exception handler tests              |

## 🏗️ System Architecture

```mermaid
graph TD
    A[API Layer] --> B[Service Layer]
    B --> C[Domain Model]
    C --> D[Repository Layer]
    D --> E[Database]
    C --> F[Event Publisher]
    F --> G[Subscribers]
    F --> H[Legacy Systems]
```

## 📦 Data Model Highlights

### Core Entities

#### Achievement
```java
@Entity
@Table(name = "achievements")
public class Achievement {
    @Id
    private String achievementId; // UUID
    private String name;
    private String description;
    @Type(JsonType.class)
    private JsonNode criteria; // JSON criteria
    private LocalDateTime createdAt;
}
```

#### LadderLevel
```java
@Entity
@Table(name = "ladder_levels")
public class LadderLevel {
    @Id
    private int level;
    private String label;
    private int pointsRequired;
    private LocalDateTime createdAt;
}
```

#### PointsEarnedEvent
```java
public class PointsEarnedEvent extends DomainEvent {
    private final int points;
    private final int newTotal;
    private final String source;
    private final JsonNode metadata;
}
```

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License
Distributed under the MIT License. See `LICENSE` for more information.

---

**📬 Contact**  
Project Maintainer - [Your Name](mailto:your.email@example.com)  
Project Link - [https://github.com/your-org/gamify-demo](https://github.com/your-org/gamify-demo)
