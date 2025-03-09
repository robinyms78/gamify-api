# Achievement Tracking Feature

## Overview

The Achievement Tracking feature allows employees to view the achievements they've earned, track their progress, and be recognized for specific milestones. The system awards achievements based on predefined criteria and provides endpoints for users to view their earned achievements.

## Architecture

The Achievement Tracking feature is implemented using several design patterns to ensure maintainability, extensibility, and testability:

### Design Patterns Used

1. **Strategy Pattern**: Used for evaluating different types of achievement criteria.
2. **Factory Pattern**: Used for creating Achievement and UserAchievement objects.
3. **Observer Pattern**: Used for processing achievements based on events.
4. **Facade Pattern**: Used to simplify the interaction between controllers and multiple services.
5. **DTO Pattern**: Used for transferring achievement data between layers and to the client.

### Components

#### Models
- `Achievement`: Represents an achievement that users can earn.
- `UserAchievement`: Represents an achievement earned by a user.
- `UserAchievementId`: Composite key for the UserAchievement entity.

#### DTOs
- `AchievementDTO`: Data Transfer Object for Achievement information.
- `UserAchievementDTO`: Data Transfer Object for User Achievement information.

#### Strategies
- `AchievementCriteriaStrategy`: Strategy interface for evaluating different types of achievement criteria.
- `PointsThresholdStrategy`: Strategy for evaluating points threshold criteria.
- `TaskCompletionStrategy`: Strategy for evaluating task completion criteria.
- `ConsecutiveDaysStrategy`: Strategy for evaluating consecutive days criteria.
- `AchievementCriteriaEvaluator`: Evaluator that uses the Strategy pattern to delegate evaluation to the appropriate strategy.

#### Factories
- `AchievementFactory`: Factory for creating Achievement objects.
- `UserAchievementFactory`: Factory for creating UserAchievement objects.

#### Events
- `EventPublisher`: Interface for publishing events in the system.
- `EventListener`: Interface for listening to events in the system.
- `DefaultEventPublisher`: Default implementation of the EventPublisher interface.
- `AchievementProcessor`: Event listener for processing achievements based on events.

#### Services
- `AchievementService`: Service interface for managing achievements.
- `AchievementServiceImpl`: Implementation of the AchievementService interface.
- `UserAchievementService`: Service interface for managing user achievements.
- `UserAchievementServiceImpl`: Implementation of the UserAchievementService interface.

#### Facade
- `GamificationFacade`: Facade for gamification-related operations.

#### Controllers
- `AchievementController`: REST controller for achievement-related endpoints.
- `UserAchievementController`: REST controller for user achievement-related endpoints.

## Flow

1. The system publishes events when users complete certain actions (e.g., completing tasks, earning points).
2. The `AchievementProcessor` listens for these events and processes achievements accordingly.
3. The `AchievementCriteriaEvaluator` evaluates whether a user meets the criteria for an achievement.
4. If the criteria are met, the `UserAchievementService` awards the achievement to the user.
5. Users can view their earned achievements via the REST endpoints provided by the controllers.

## API Endpoints

### Achievement Endpoints

- `GET /api/achievements`: Get all achievements.
- `GET /api/achievements/{achievementId}`: Get an achievement by its ID.
- `POST /api/achievements`: Create a new achievement.
- `PUT /api/achievements/{achievementId}`: Update an existing achievement.
- `DELETE /api/achievements/{achievementId}`: Delete an achievement.
- `GET /api/achievements/user/{userId}`: Get a user's achievements.
- `GET /api/achievements/{achievementId}/check/{userId}`: Check if a user has a specific achievement.
- `POST /api/achievements/process/{userId}`: Process an event for a user.

### User Achievement Endpoints

- `GET /api/users/{userId}/achievements`: Get all achievements earned by a user.
- `GET /api/users/{userId}/achievements/count`: Get the count of achievements earned by a user.
- `GET /api/users/{userId}/achievements/{achievementId}`: Check if a user has a specific achievement.

## Achievement Criteria Types

The system supports the following types of achievement criteria:

1. **Points Threshold**: Awarded when a user earns a certain number of points.
2. **Task Completion Count**: Awarded when a user completes a certain number of tasks.
3. **Consecutive Days**: Awarded when a user is active for a consecutive number of days.

## Example Achievement JSON

```json
{
  "name": "Task Master",
  "description": "Complete 10 tasks",
  "criteria": {
    "type": "TASK_COMPLETION_COUNT",
    "count": 10,
    "eventType": "TASK_COMPLETED"
  }
}
```

## Example User Achievement JSON

```json
{
  "userId": "user123",
  "username": "johndoe",
  "achievements": [
    {
      "id": "achievement123",
      "name": "Task Master",
      "description": "Complete 10 tasks",
      "earnedAt": "2023-01-01T12:00:00",
      "metadata": {
        "eventType": "TASK_COMPLETED",
        "eventData": {
          "taskId": "task123",
          "taskName": "Complete Project"
        }
      },
      "earned": true
    }
  ],
  "totalAchievements": 10,
  "earnedAchievements": 1
}
```

## Benefits of the Refactored Implementation

1. **Improved Maintainability**: Clear separation of concerns with each class having a single responsibility.
2. **Enhanced Extensibility**: Easy to add new achievement criteria types by implementing the `AchievementCriteriaStrategy` interface.
3. **Better Testability**: Smaller, focused components that are easier to test.
4. **Reduced Coupling**: Components interact through well-defined interfaces.
5. **Scalability**: Event-driven architecture allows for better scaling.
