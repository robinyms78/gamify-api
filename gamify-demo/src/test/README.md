# Gamify API Test Suite

This directory contains comprehensive test scripts for testing various features of the Gamify API.

## Test Suites

The project includes the following test suites:

1. **User Test Suite** - Tests for User CRUD operations
2. **Leaderboard Test Suite** - Tests for Leaderboard functionality

## User Test Suite

The User test suite is organized into the following layers:

1. **Repository Layer Tests** - `UserRepositoryTest.java`
   - Tests the data access layer using JPA and H2 in-memory database
   - Verifies CRUD operations directly on the database

2. **Service Layer Tests** - `UserServiceTest.java`
   - Tests the business logic layer with mocked repository
   - Verifies service methods handle data correctly and throw appropriate exceptions

3. **Validator Tests** - `UserValidatorTest.java`
   - Tests the validation logic for User objects
   - Verifies validation rules are correctly applied

4. **Controller Layer Tests** - `UserControllerTest.java`
   - Tests the REST API endpoints using MockMvc
   - Verifies HTTP responses, status codes, and JSON payloads

5. **Integration Tests** - `UserIntegrationTest.java`
   - Tests the full flow from controller to repository
   - Verifies end-to-end functionality with an in-memory database

## Leaderboard Test Suite

The Leaderboard test suite is organized into the following layers:

1. **Unit Tests**
   - `LeaderboardServiceImplTest.java` - Tests the business logic in the service layer
   - `LeaderboardMapperTest.java` - Tests the mapping between entities and DTOs
   - `LeaderboardControllerTest.java` - Tests the controller endpoints with mocked service and mapper
   - `LeaderboardSchedulerTest.java` - Tests the scheduler functionality

2. **Integration Tests**
   - `LeaderboardRepositoryIntegrationTest.java` - Tests the repository layer with a real database
   - `LeaderboardServiceIntegrationTest.java` - Tests the service layer with real repositories

3. **End-to-End Tests**
   - `LeaderboardApiTest.java` - Tests the API endpoints with real HTTP requests

For more details on the Leaderboard tests, see the [Leaderboard Testing Guide](leaderboard-tests-README.md).

## Running the Tests

### Running All Tests

To run all tests in the project:

```bash
mvn test
```

### Running Specific Test Suites

To run specific test suites:

```bash
# Run User test suite
mvn test -Dtest=UserTestSuite

# Run Leaderboard test suite
mvn test -Dtest=LeaderboardTestSuite
```

### Running Individual Test Classes

To run a specific test class:

```bash
# User tests
mvn test -Dtest=UserRepositoryTest
mvn test -Dtest=UserServiceTest
mvn test -Dtest=UserValidatorTest
mvn test -Dtest=UserControllerTest
mvn test -Dtest=UserIntegrationTest

# Leaderboard tests
mvn test -Dtest=LeaderboardServiceImplTest
mvn test -Dtest=LeaderboardMapperTest
mvn test -Dtest=LeaderboardControllerTest
mvn test -Dtest=LeaderboardSchedulerTest
mvn test -Dtest=LeaderboardRepositoryIntegrationTest
mvn test -Dtest=LeaderboardServiceIntegrationTest
mvn test -Dtest=LeaderboardApiTest
```

### Using Test Scripts

The project includes shell scripts to run specific test suites:

```bash
# Run User tests
./run-user-tests.sh

# Run Leaderboard tests
./run-leaderboard-tests.sh
```

## Test Configuration

The tests use an H2 in-memory database configured in `application-test.properties`. This ensures that tests run in isolation without affecting any external databases.

## Test Coverage

### User Test Coverage

The User test suite covers:

- **Create operations**: Creating new users with valid and invalid data
- **Read operations**: Retrieving users by ID, username, email, and getting all users
- **Update operations**: Updating existing users with valid and invalid data
- **Delete operations**: Deleting users by ID

Each operation is tested for both success scenarios and failure scenarios (e.g., not found, validation errors).

### Leaderboard Test Coverage

The Leaderboard test suite covers:

- **Service Layer**: Calculating ranks, handling ties, retrieving rankings, filtering by department, getting top users, creating and updating entries
- **Mapper Layer**: Converting entities to DTOs, handling null values, converting lists and pages
- **Controller Layer**: API endpoints for retrieving rankings, filtering, getting user ranks, getting top users, recalculating ranks
- **Repository Layer**: Finding entries by user, finding all entries, filtering by department, finding top users
- **Scheduler Layer**: Periodic rank recalculation

For more details on the test coverage, see the individual test suite README files.
