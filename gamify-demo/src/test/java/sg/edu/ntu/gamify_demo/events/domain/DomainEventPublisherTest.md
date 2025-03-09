# Domain Event Publisher Test Documentation

## 📋 Test Overview

| Aspect               | Details                                                                 |
|----------------------|-------------------------------------------------------------------------|
| **Test Class**       | `sg.edu.ntu.gamify_demo.events.domain.DomainEventPublisherTest`         |
| **Test Type**        | Unit Tests (Behavior Verification)                                      |
| **Test Framework**   | JUnit 5                                                                 |
| **Mocking**          | Mockito                                                                 |
| **Key Components**   | `DomainEventPublisher`, `EventPublisher` (legacy), `DomainEventSubscriber` |

## 🧩 Key Test Components

| Component               | Purpose                                                                 |
|-------------------------|-------------------------------------------------------------------------|
| `legacyEventPublisher`  | Mock legacy system to verify backward compatibility                    |
| `testSubscriber`        | Mock subscriber to verify event reception                             |
| `testUser`              | Test user with pre-configured points (100 earned/available)           |
| `objectMapper`          | JSON serializer for event data conversion                             |

## 🧪 Test Case Specifications

### 1. Task Completion Event Handling
**Test Method**: `testPublish_TaskCompletedEvent()`  
**Scenario**:  
- User completes a task with metadata
- System awards 30 points  

**Verifications**:
```java
// Subscriber notification
verify(testSubscriber, times(1)).onEvent(event);

// Legacy system data integrity
assertEquals("task123", capturedData.get("taskId").asText());
assertEquals("event123", capturedData.get("eventId").asText());
assertEquals(30, capturedData.get("pointsAwarded").asInt());
assertEquals("HIGH", capturedData.get("metadata").get("priority").asText());
```

### 2. Points Earning Event Propagation
**Test Method**: `testPublish_PointsEarnedEvent()`  
**Scenario**:  
- User earns 50 points (total becomes 150)  
- Source: "TEST_SOURCE"  

**Verifications**:
```java
// Points calculation accuracy
assertEquals(50, capturedData.get("points").asInt());
assertEquals(150, capturedData.get("newTotal").asInt());

// Metadata persistence
assertEquals("TEST_SOURCE", capturedData.get("source").asText());
assertEquals("test", capturedData.get("metadata").get("source").asText());
```

### 3. Points Spending Validation
**Test Method**: `testPublish_PointsSpentEvent()`  
**Scenario**:  
- User spends 30 points (balance reduces to 70)  
- Source: "TEST_SPEND"  

**Verifications**:
```java
// Balance deduction correctness
assertEquals(30, capturedData.get("points").asInt());
assertEquals(70, capturedData.get("newTotal").asInt());

// Source tracking
assertEquals("TEST_SPEND", capturedData.get("source").asText());
```

## ⚙️ Test Implementation Details

### Custom Assertion Logic
```java
private void assertEquals(Object expected, Object actual) {
    if (!expected.equals(actual)) {
        throw new AssertionError("Expected " + expected + " but got " + actual);
    }
}
```
*Purpose*: Provides explicit failure messages for data validation

### Test Setup Flow
```java
@BeforeEach
public void setup() {
    // 1. Mock legacy publisher and subscriber
    // 2. Configure JSON serializer
    // 3. Initialize test user with 100 points
    // 4. Register subscriber with publisher
}
```

## 🔍 Coverage Analysis

| Aspect                | Covered? | Details                                  |
|-----------------------|----------|------------------------------------------|
| Event Types           | ✅       | 3 main event types tested               |
| Data Serialization    | ✅       | Full JSON structure validation          |
| Legacy Integration    | ✅       | Backward-compatible event format        |
| Metadata Handling     | ✅       | Complex JSON node verification          |
| Point Calculations    | ✅       | Earned/spent/balance transitions        |

## 📊 Recommended Additional Tests
1. Concurrent event publishing
2. Invalid event type handling
3. Subscriber unregistration scenarios
4. High-volume event throughput
