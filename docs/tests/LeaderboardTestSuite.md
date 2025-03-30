# Leaderboard Test Suite Documentation

## Overview

The Leaderboard Test Suite provides comprehensive testing for the leaderboard functionality in the Gamify API. It includes unit tests, integration tests, and end-to-end tests to ensure the leaderboard feature works correctly at all levels.

## Test Structure

The test suite is organized into the following layers:

### Unit Tests

- **LeaderboardServiceImplTest**: Tests the business logic in the service layer
  - Tests rank calculation based on points
  - Tests handling of ties in ranking
  - Tests pagination of global rankings
  - Tests filtering by department
  - Tests retrieval of user ranks
  - Tests retrieval of top users
  - Tests creation and updating of leaderboard entries

- **LeaderboardMapperTest**: Tests the mapping between entities and DTOs
  - Tests conversion of entities to DTOs
  - Tests handling of null values
  - Tests conversion of lists and pages of entities

- **LeaderboardControllerTest**: Tests the controller endpoints with mocked service and mapper
  - Tests retrieval of global rankings
  - Tests filtering by department
  - Tests retrieval of user ranks
  - Tests retrieval of top users
  - Tests recalculation of ranks
  - Tests error handling

- **LeaderboardSchedulerTest**: Tests the scheduler functionality
  - Tests periodic rank recalculation
  - Tests error handling during scheduled tasks

### Integration Tests

- **LeaderboardRepositoryIntegrationTest**: Tests the repository layer with a real database
  - Tests finding entries by user
  - Tests finding all entries ordered by rank
  - Tests filtering by department
  - Tests finding top users

- **LeaderboardServiceIntegrationTest**: Tests the service layer with real repositories
  - Tests rank calculation with real database interactions
  - Tests creation and updating of leaderboard entries
  - Tests transaction management

### End-to-End Tests

- **LeaderboardApiTest**: Tests the API endpoints with real HTTP requests
  - Tests retrieval of global rankings
  - Tests filtering by department
  - Tests retrieval of user ranks
  - Tests retrieval of top users
  - Tests recalculation of ranks
  - Tests authentication requirements

## Test Data

The tests use the following test data:

- **Users**: Three test users with different points and departments
- **Ladder Levels**: Two ladder levels (Beginner and Intermediate)
- **Leaderboard Entries**: Entries for each user with ranks based on points

## Running the Tests

To run all leaderboard tests at once, use the provided shell script:

```bash
./src/test/run-leaderboard-tests.sh
```

This script will run the `LeaderboardTestSuite` which includes all unit tests, integration tests, and end-to-end tests for the leaderboard feature.

To run specific test classes:

```bash
mvn test -Dtest=LeaderboardServiceImplTest
mvn test -Dtest=LeaderboardMapperTest
mvn test -Dtest=LeaderboardControllerTest
mvn test -Dtest=LeaderboardSchedulerTest
mvn test -Dtest=LeaderboardRepositoryIntegrationTest
mvn test -Dtest=LeaderboardServiceIntegrationTest
mvn test -Dtest=LeaderboardApiTest
```

## Test Coverage

The test suite covers the following functionality:

- **Service Layer**: Calculating ranks, handling ties, retrieving rankings, filtering by department, getting top users, creating and updating entries
- **Mapper Layer**: Converting entities to DTOs, handling null values, converting lists and pages
- **Controller Layer**: API endpoints for retrieving rankings, filtering, getting user ranks, getting top users, recalculating ranks
- **Repository Layer**: Finding entries by user, finding all entries, filtering by department, finding top users
- **Scheduler Layer**: Periodic rank recalculation

## Dependencies

The tests depend on the following components:

- **Spring Boot Test**: For testing Spring Boot applications
- **JUnit 5**: For writing and running tests
- **Mockito**: For mocking dependencies in unit tests
- **H2 Database**: For in-memory database testing
- **Spring Security Test**: For testing secured endpoints

## Known Issues

- None currently identified

## Future Improvements

- Add performance tests for large datasets
- Add load tests for concurrent users
- Add more edge case tests for tie-breaking scenarios
