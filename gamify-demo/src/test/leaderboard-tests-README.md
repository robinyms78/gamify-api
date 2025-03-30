# Leaderboard Testing Guide

This guide provides information on the testing strategy implemented for the leaderboard feature in the Gamify API project.

## Testing Strategy

The leaderboard feature has been tested using a comprehensive approach that includes:

1. **Unit Tests**: Testing individual components in isolation with mocked dependencies
2. **Integration Tests**: Testing interactions between components with real repositories
3. **End-to-End Tests**: Testing API endpoints with real HTTP requests

## Test Structure

The tests are organized as follows:

### Unit Tests

- `LeaderboardServiceImplTest`: Tests the business logic in the service layer
- `LeaderboardMapperTest`: Tests the mapping between entities and DTOs
- `LeaderboardControllerTest`: Tests the controller endpoints with mocked service and mapper
- `LeaderboardSchedulerTest`: Tests the scheduler functionality

### Integration Tests

- `LeaderboardRepositoryIntegrationTest`: Tests the repository layer with a real database
- `LeaderboardServiceIntegrationTest`: Tests the service layer with real repositories

### End-to-End Tests

- `LeaderboardApiTest`: Tests the API endpoints with real HTTP requests

## Running the Tests

### Running All Leaderboard Tests

To run all leaderboard tests at once, use the provided shell script:

```bash
./src/test/run-leaderboard-tests.sh
```

This script will run the `LeaderboardTestSuite` which includes all unit tests, integration tests, and end-to-end tests for the leaderboard feature.

### Running Specific Test Categories

To run only specific categories of tests, you can use Maven with the appropriate test class:

```bash
# Run only unit tests for the service layer
mvn test -Dtest=sg.edu.ntu.gamify_demo.services.LeaderboardServiceImplTest

# Run only integration tests for the repository layer
mvn test -Dtest=sg.edu.ntu.gamify_demo.repositories.LeaderboardRepositoryIntegrationTest

# Run only end-to-end tests
mvn test -Dtest=sg.edu.ntu.gamify_demo.integration.LeaderboardApiTest
```

## Test Coverage

The tests cover the following functionality:

### Service Layer

- Calculating ranks based on points
- Handling ties in ranking
- Retrieving global rankings with pagination
- Filtering rankings by department
- Getting a user's rank
- Getting top users
- Creating and updating leaderboard entries

### Mapper Layer

- Converting entities to DTOs
- Handling null values
- Converting lists and pages of entities

### Controller Layer

- Retrieving global rankings
- Filtering rankings by department
- Getting a user's rank
- Getting top users
- Recalculating ranks
- Error handling

### Repository Layer

- Finding entries by user
- Finding all entries ordered by rank
- Filtering entries by department
- Finding top users

## Test Data

The tests use the following test data:

- **Users**: Three test users with different points and departments
- **Ladder Levels**: Two ladder levels (Beginner and Intermediate)
- **Leaderboard Entries**: Entries for each user with ranks based on points

## Test Environment

The tests use the following environment:

- **Database**: H2 in-memory database for unit tests, PostgreSQL for integration tests
- **Spring Profiles**: The `test` profile is used for all tests
- **Security**: Mock users are used for authenticated endpoints

## Adding New Tests

When adding new functionality to the leaderboard feature, follow these guidelines:

1. Add unit tests for new business logic
2. Add integration tests for new repository methods
3. Add end-to-end tests for new API endpoints
4. Update the test suite to include new test classes
