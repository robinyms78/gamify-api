# Task Completion & Points Awarding Feature Tests

This directory contains test scripts for verifying the Task Completion & Points Awarding feature of the Gamify API. These tests are designed to validate the user story:

> As an employee, I want to complete a task and automatically earn points so that my performance is recognized and recorded.

## Test Scripts

The tests are broken down into small, focused pieces to avoid overwhelming the console with too many messages:

1. **Task Event Record Creation Test** (`task-event-record-creation-test.sh`)
   - Verifies that a new record is created in task_events with event_type 'TASK_COMPLETED'
   - Checks that the task event contains the correct user, task ID, and status

2. **Points Calculation Test** (`points-calculation-test.sh`)
   - Tests points calculation for different priority levels:
     - LOW: 10 points
     - MEDIUM: 20 points
     - HIGH: 30 points
     - CRITICAL: 50 points
     - DEFAULT (no priority): 15 points

3. **Points Transaction Test** (`points-transaction-test.sh`)
   - Verifies that the user's earned_points increase by the appropriate value
   - Checks that a corresponding transaction is recorded in points_transactions

4. **Ladder Status Update Test** (`ladder-status-update-test.sh`)
   - Verifies that if the user's earned_points cross a threshold, their ladder status is updated

5. **Master Test Script** (`run-task-completion-tests.sh`)
   - Runs all the individual test scripts in sequence
   - Reports the overall test results

## Prerequisites

Before running the tests, ensure that:

1. The Gamify API server is running on http://localhost:8080
2. The database is properly set up with the necessary tables
3. The test scripts have execute permissions

## Running the Tests

### Running All Tests

To run all tests at once, use the master test script:

```bash
cd src/test
chmod +x run-task-completion-tests.sh
./run-task-completion-tests.sh
```

### Running Individual Tests

You can also run each test individually:

```bash
cd src/test
chmod +x task-event-record-creation-test.sh
./task-event-record-creation-test.sh

chmod +x points-calculation-test.sh
./points-calculation-test.sh

chmod +x points-transaction-test.sh
./points-transaction-test.sh

chmod +x ladder-status-update-test.sh
./ladder-status-update-test.sh
```

## Test Results

Each test script will output detailed information about the test steps and results. The output is color-coded:

- **Green**: Success messages
- **Red**: Error messages
- **Yellow**: Information messages
- **Blue**: Section headers (in the master script)

If a test fails, it will exit with a non-zero status code and display an error message explaining what went wrong.

## Troubleshooting

If the tests fail, check the following:

1. Ensure the Gamify API server is running and accessible at http://localhost:8080
2. Verify that the database is properly set up and accessible
3. Check the server logs for any errors
4. Make sure the test scripts have execute permissions
5. Examine the specific error message to identify the issue

## Modifying the Tests

If you need to modify the tests:

1. Each test script is self-contained and can be modified independently
2. The BASE_URL variable at the top of each script can be changed if the API is running on a different host or port
3. The test data (usernames, emails, task IDs, etc.) is generated dynamically to avoid conflicts
4. The expected points values are based on the current implementation of the points calculation strategy
