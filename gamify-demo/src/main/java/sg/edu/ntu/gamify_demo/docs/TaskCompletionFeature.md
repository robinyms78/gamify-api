# Task Completion & Points Awarding Feature

This document describes the implementation of the Task Completion & Points Awarding feature, which allows employees to earn points automatically when they complete tasks.

## Overview

When an employee completes a task in an external project management tool, the external system sends a task completion event to the Gamify API. The system then:

1. Logs the task event
2. Calculates points based on task priority
3. Updates the user's earned points and available points
4. Records the transaction in the points_transactions table
5. Updates the user's ladder status if they cross a threshold

## API Endpoint

### POST /tasks/events

This endpoint processes task events from external systems.

#### Request Body

```json
{
  "userId": "user123",
  "taskId": "task456",
  "event_type": "TASK_COMPLETED",
  "data": {
    "priority": "HIGH",
    "description": "Complete project documentation"
  }
}
```

#### Required Fields

- `userId`: The ID of the user who completed the task
- `taskId`: The ID of the task that was completed
- `event_type`: The type of event (must be "TASK_COMPLETED" for task completion)

#### Optional Fields in `data`

- `priority`: The priority of the task (LOW, MEDIUM, HIGH, CRITICAL)
- `description`: A description of the task
- Other task-specific metadata

#### Response

```json
{
  "success": true,
  "eventId": "evt_123456789",
  "userId": "user123",
  "taskId": "task456",
  "eventType": "TASK_COMPLETED",
  "status": "COMPLETED",
  "pointsAwarded": 30,
  "priority": "HIGH"
}
```

## Points Calculation

Points are awarded based on the priority of the task:

- LOW: 10 points
- MEDIUM: 20 points
- HIGH: 30 points
- CRITICAL: 50 points
- DEFAULT (if priority not specified): 15 points

## Database Changes

When a task completion event is processed, the following database changes occur:

1. A new record is created in the `task_events` table with `event_type` set to "TASK_COMPLETED"
2. The user's `earned_points` and `available_points` are increased by the calculated points
3. A new record is created in the `points_transactions` table with the transaction details
4. If the user's points cross a threshold, their ladder status is updated in the `user_ladder_status` table

## Example Usage

### Using cURL

```bash
curl -X POST http://localhost:8080/tasks/events \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "taskId": "task456",
    "event_type": "TASK_COMPLETED",
    "data": {
      "priority": "HIGH",
      "description": "Complete project documentation"
    }
  }'
```

### Using Postman

1. Create a new POST request to `http://localhost:8080/tasks/events`
2. Set the Content-Type header to `application/json`
3. In the request body, select "raw" and "JSON", then enter the JSON payload as shown above
4. Send the request

## Integration with External Systems

External systems can integrate with this feature by sending HTTP POST requests to the `/tasks/events` endpoint whenever a task is completed. The external system should include the user ID, task ID, and any relevant task metadata in the request.
