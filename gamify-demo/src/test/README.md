# User CRUD Operations Test Suite

This directory contains comprehensive test scripts for testing the User class CRUD operations in the Gamify API.

## Test Structure

The test suite is organized into the following layers:

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

## Running the Tests

### Running All Tests

To run all tests in the suite:

```bash
mvn test
```

### Running the Test Suite

To run the User test suite specifically:

```bash
mvn test -Dtest=UserTestSuite
```

### Running Individual Test Classes

To run a specific test class:

```bash
mvn test -Dtest=UserRepositoryTest
mvn test -Dtest=UserServiceTest
mvn test -Dtest=UserValidatorTest
mvn test -Dtest=UserControllerTest
mvn test -Dtest=UserIntegrationTest
```

## Test Configuration

The tests use an H2 in-memory database configured in `application-test.properties`. This ensures that tests run in isolation without affecting any external databases.

## Test Coverage

The test suite covers:

- **Create operations**: Creating new users with valid and invalid data
- **Read operations**: Retrieving users by ID, username, email, and getting all users
- **Update operations**: Updating existing users with valid and invalid data
- **Delete operations**: Deleting users by ID

Each operation is tested for both success scenarios and failure scenarios (e.g., not found, validation errors).
