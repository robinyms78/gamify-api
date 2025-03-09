# Test Batching and Logging System

This document explains how to use the new test batching and logging system for the Gamify API project.

## Overview

The test batching system breaks down the test suite into smaller groups of tests (max 5 tests per group) to:

1. Make test output more manageable
2. Provide better visibility into test failures
3. Log test results to separate files for easier tracking
4. Generate a summary report of all test results

## Configuration

The system consists of:

1. **Maven Surefire Plugin Configuration**: Configured in `pom.xml` to support test grouping and logging
2. **Custom Logback Configuration**: Defined in `src/test/resources/logback-test.xml` to direct logs to group-specific files
3. **Test Batching Script**: Located at `src/test/run-tests-in-batches.sh` to run tests in smaller batches

## How to Run Tests

### Running All Tests in Batches

To run all tests in smaller batches with separate logs:

```bash
cd /path/to/gamify-api
./gamify-demo/src/test/run-tests-in-batches.sh
```

This will:
- Run each test group separately
- Log the output to separate files in `target/test-logs/`
- Generate a summary report at `target/test-logs/summary.txt`
- Display the summary in the console

### Running Specific Test Groups

To run a specific test group:

```bash
cd /path/to/gamify-api
mvn test -Dtest.group=GroupName -Dtest="sg.edu.ntu.gamify_demo.package.TestClass"
```

For example:
```bash
mvn test -Dtest.group=UserService -Dtest="sg.edu.ntu.gamify_demo.services.UserServiceTest"
```

## Test Groups

Tests are organized into the following groups:

### User-related Tests
- UserRepo: User Repository Tests
- UserService: User Service Tests
- UserController: User Controller Tests
- UserIntegration: User Integration Tests
- AuthTests: Authentication Tests

### Achievement-related Tests
- AchievementStrategies: Achievement Strategies Tests
- AchievementService: Achievement Service Tests
- UserAchievementService: User Achievement Service Tests
- AchievementIntegration: Achievement Integration Tests

### Task-related Tests
- TaskEventController: Task Event Controller Tests
- TaskEventIntegration: Task Event Integration Tests
- TaskStrategies: Task Strategies Tests

### Ladder/Leaderboard-related Tests
- LadderService: Ladder Status Service Tests
- LadderController: Ladder Controller Tests
- LadderIntegration: Ladder Status Integration Tests

### Points/Transaction-related Tests
- PointsService: Points Service Tests
- PointsEvents: Points Event Subscriber Tests

### Domain Event-related Tests
- DomainEvents: Domain Event Publisher Tests
- EventSubscribers: Event Subscribers Tests

## Log Files

Test logs are stored in the following locations:

- **Individual Test Group Logs**: `target/test-logs/{test.group}.log`
- **Summary Report**: `target/test-logs/summary.txt`

## Customizing Test Groups

To modify or add test groups:

1. Edit the `run-tests-in-batches.sh` script
2. Add or modify the `run_command` calls with appropriate test patterns

Example:
```bash
run_command "mvn test -Dtest.group=NewGroup -Dtest=\"sg.edu.ntu.gamify_demo.package.NewTest\"" "New Test Group"
```

## Troubleshooting

If you encounter issues:

1. **Missing Log Directory**: The script should create the log directory automatically, but you can create it manually:
   ```bash
   mkdir -p target/test-logs
   ```

2. **Permission Issues**: Ensure the script is executable:
   ```bash
   chmod +x gamify-demo/src/test/run-tests-in-batches.sh
   ```

3. **Test Pattern Issues**: If tests aren't being found, check the test patterns in the script and adjust as needed.
