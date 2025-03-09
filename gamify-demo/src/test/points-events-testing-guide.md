# Points Events Testing Guide

This guide explains how to test the refactored points events functionality in the gamify-demo application.

## Overview

The points-related event handling has been refactored to use explicit domain event classes (`PointsEarnedEvent` and `PointsSpentEvent`) instead of string-based event types. This improves type safety, maintainability, and extensibility.

## Running the Tests

We've created a script to run all the tests related to the points events refactoring:

```bash
# Make the script executable (if not already)
chmod +x src/test/run-points-events-tests.sh

# Run the tests
./src/test/run-points-events-tests.sh
```

This script will run:
1. Unit tests for the `PointsService` class
2. Unit tests for the `PointsEventSubscriber` class
3. Unit tests for the `DomainEventPublisher` class
4. Integration tests for task completion (which involves points awarding)

## Manual Testing

You can also manually test the points events functionality:

### Testing Points Earned Events

1. Create a test user:
   ```bash
   ./src/test/create-test-user.sh
   ```

2. Complete a task to earn points:
   ```bash
   ./src/test/task-completion-test.sh
   ```

3. Check the logs to verify that the `PointsEventSubscriber` logged the points earned event:
   ```bash
   # Look for log entries like:
   # "User testuser earned 30 points from TASK_COMPLETED. New total: 30"
   ```

### Testing Points Spent Events

Currently, there's no direct API endpoint to spend points, but you can test this functionality by:

1. Creating a test user with points (by completing tasks)
2. Using the application to redeem rewards or perform other actions that spend points
3. Checking the logs to verify that the `PointsEventSubscriber` logged the points spent event

## What to Look For

When testing, verify that:

1. Points are correctly awarded and spent
2. The appropriate domain events are published
3. The `PointsEventSubscriber` correctly logs the events
4. The legacy event system still works (for backward compatibility)
5. The existing functionality (like task completion) still works as expected

## Troubleshooting

If you encounter issues:

1. Check the application logs for error messages
2. Verify that the `PointsEventSubscriber` is registered in `EventConfig`
3. Ensure that the `DomainEventPublisher` is correctly handling the new event types
4. Check that the `PointsService` is correctly creating and publishing the domain events

## Next Steps

After verifying that the refactored code works as expected, consider:

1. Adding more specific subscribers for points-related events
2. Making event processing asynchronous for better performance
3. Persisting events for audit and replay purposes
4. Implementing versioning for events to handle schema changes
