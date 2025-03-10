# Core User Workflows

## Table of Contents
1. [User Registration](#1-user-registration-process)
2. [Task Completion & Points](#2-task-completion--points-flow)
3. [Achievements & Status](#3-viewing-achievements--ladder-status)
4. [System Limitations](#system-wide-limitations)

---

## 1. User Registration Process

### Workflow
```http
POST /api/users
```
1. Submit registration with:
   - Unique username
   - Valid email
   - Password (automatically hashed)

2. System validations:
   - Email format check
   - `UserRepository.existsByEmail()`
   - `UserRepository.existsByUsername()`

3. Post-registration setup:
   ```java
   User.builder().earnedPoints(0).build();
   userLadderStatusRepository.initializeStatus(userId);
   ```

### Prerequisites
- Email verification system (external)
- Password complexity rules enforced by `UserValidator`

---

## 2. Task Completion & Points Flow

### Event Processing
```http
POST /tasks/events
```
1. Command chain execution:
   ```java
   new CompositeTaskCommand(
     calculatePointsCommand,
     recordTransactionCommand,
     updateLadderStatusCommand
   ).execute();
   ```

2. Points lifecycle:
   ```mermaid
   graph LR
   A[TaskEvent] --> B(CalculatePointsStrategy)
   B --> C{PointsTransaction}
   C --> D[User.earnedPoints]
   D --> E[Leaderboard.syncWithUser()]
   ```

### Limitations
- Points strategies bound to task types
- No partial points awarded
- Transaction audit trail (24h window)

---

## 3. Viewing Achievements & Ladder Status

### Achievement Endpoints
```http
GET /api/achievements
GET /api/achievements/{id}
```
- Criteria evaluation via `AchievementProcessor`
- Earned achievements stored in `UserAchievement`

### Ladder Progression
```http
GET /api/ladder/users/{userId}
```
- Level calculation:
  ```java
  ladderStatusService.getUserLadderStatus(userId);
  ```
- Progress tracking uses `PointsCalculationStrategy`

---

## System-Wide Limitations

1. **Level Cap**  
   Maximum level defined by `LadderLevelRepository` entries

2. **Achievement Immutability**  
   Criteria locked after creation

3. **Event Processing**  
   Single-event only (no bulk operations)

4. **Points Economy**  
   Spending requires custom implementation
