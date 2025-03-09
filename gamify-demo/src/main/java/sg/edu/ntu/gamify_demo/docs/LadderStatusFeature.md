# Ladder Status Feature

## Overview

The Ladder Status feature allows employees to view their current level in the gamification ladder system, along with their total earned points and the points needed to reach the next level.

## User Story

As an employee, I want to view my ladder status so that I can see my current level, my total earned points, and the points needed for the next level.

### Scenario
- **Given**: Sarah is actively completing tasks.
- **When**: She queries the `/api/ladder/status?userId=SarahID` endpoint.
- **Then**: The API returns her current level (from user_ladder_status), total earned_points, and points_to_next_level based on thresholds defined in ladder_levels.

### Acceptance Criteria
- Response includes fields: current_level, earned_points, points_to_next_level, and ladder level label.
- Ladder thresholds are correctly applied from the ladder_levels table.

## API Endpoints

### Get Ladder Status

```
GET /api/ladder/status?userId={userId}
```

#### Request Parameters

| Parameter | Type   | Required | Description                |
|-----------|--------|----------|----------------------------|
| userId    | String | Yes      | The ID of the user         |

#### Response

```json
{
  "currentLevel": 2,
  "levelLabel": "Intermediate",
  "earnedPoints": 250,
  "pointsToNextLevel": 150
}
```

#### Response Fields

| Field            | Type    | Description                                   |
|------------------|---------|-----------------------------------------------|
| currentLevel     | Integer | The user's current level in the ladder        |
| levelLabel       | String  | The human-readable label for the current level|
| earnedPoints     | Integer | The total points earned by the user           |
| pointsToNextLevel| Integer | The points needed to reach the next level     |

#### Error Responses

| Status Code | Description       | Response Body                                      |
|-------------|-------------------|---------------------------------------------------|
| 404         | User not found    | `{"error": "User not found", "message": "..."}`    |

## Implementation Details

The Ladder Status feature is implemented using the following design patterns:

1. **DTO Pattern**: Uses `LadderStatusDTO` to transfer data between layers.
2. **Strategy Pattern**: Uses `PointsCalculationStrategy` for flexible points calculation.
3. **Facade Pattern**: Uses `GamificationFacade` to simplify controller-service interaction.
4. **Service Layer Pattern**: Separates business logic in `LadderStatusService`.
5. **Repository Pattern**: Uses Spring Data JPA repositories for data access.

### Class Diagram

```
┌─────────────────┐      ┌───────────────────┐      ┌─────────────────────┐
│ LadderController│─────▶│ GamificationFacade│─────▶│ LadderStatusService │
└─────────────────┘      └───────────────────┘      └─────────────────────┘
                                                              │
                                                              ▼
┌─────────────────┐      ┌───────────────────┐      ┌─────────────────────┐
│  LadderStatusDTO│◀─────│PointsCalculation  │◀─────│LadderLevelRepository│
└─────────────────┘      │    Strategy       │      └─────────────────────┘
                         └───────────────────┘
```

## Testing

The feature includes:

1. Unit tests for the service implementation
2. Unit tests for the controller
3. Integration tests for the entire flow
4. A shell script for manual API testing

To run the manual test:

```bash
./src/test/ladder-status-test.sh
```

## Database Schema

The feature relies on the following tables:

1. `users` - Stores user information including earned points
2. `ladder_levels` - Defines the levels and points required for each level
3. `user_ladder_status` - Tracks each user's current level and progress

## Future Enhancements

Potential future enhancements for this feature:

1. Add notifications when a user levels up
2. Implement level-specific rewards or privileges
3. Add visual representation of progress (e.g., progress bar)
4. Include historical progression data
