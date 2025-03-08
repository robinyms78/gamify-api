# Achievement Tracking Testing Instructions

This document provides step-by-step instructions for running tests for the Achievement Tracking feature.

## Prerequisites

Before running the tests, ensure you have the following installed:

- Java 11 or higher
- Maven
- cURL
- jq (for JSON processing in shell scripts)

## 1. Running Unit Tests

Unit tests verify that individual components work correctly in isolation.

### Running All Unit Tests

```bash
# Navigate to the project root directory
cd gamify-demo

# Run all unit tests
mvn test

# Run tests for a specific package
mvn test -Dtest="sg.edu.ntu.gamify_demo.strategies.achievement.*Test"
```

### Running a Specific Unit Test

```bash
# Run a specific test class
mvn test -Dtest=PointsThresholdStrategyTest

# Run a specific test method
mvn test -Dtest=PointsThresholdStrategyTest#testEvaluate_UserMeetsThreshold_ReturnsTrue
```

## 2. Running Integration Tests

Integration tests verify that components work together correctly.

```bash
# Run integration tests
mvn verify -P integration-test

# Run a specific integration test
mvn verify -P integration-test -Dtest=AchievementServiceIntegrationTest
```

## 3. Running API Tests

The API tests use shell scripts to test the REST endpoints.

### Starting the Application

Before running the API tests, you need to start the application:

```bash
# Navigate to the project root directory
cd gamify-demo

# Start the application
mvn spring-boot:run
```

### Running the API Tests

Open a new terminal window and run:

```bash
# Navigate to the project root directory
cd gamify-demo

# Make the script executable
chmod +x src/test/achievement-test.sh

# Run the API tests
./src/test/achievement-test.sh
```

## 4. Checking Test Coverage

Test coverage reports show how much of your code is covered by tests.

```bash
# Generate test coverage report
mvn clean test jacoco:report

# Open the coverage report in a browser
open target/site/jacoco/index.html  # On macOS
xdg-open target/site/jacoco/index.html  # On Linux
```

## 5. Step-by-Step Testing Guide

Follow these steps to thoroughly test the Achievement Tracking feature:

### Step 1: Run Unit Tests for Strategy Components

```bash
# Run tests for all strategy components
mvn test -Dtest="sg.edu.ntu.gamify_demo.strategies.achievement.*Test"
```

Expected output:
- All tests should pass
- You should see "BUILD SUCCESS" in the output

### Step 2: Run Unit Tests for Service Components

```bash
# Run tests for service components
mvn test -Dtest="sg.edu.ntu.gamify_demo.services.*Test"
```

Expected output:
- All tests should pass
- You should see "BUILD SUCCESS" in the output

### Step 3: Run Integration Tests

```bash
# Run integration tests
mvn verify -P integration-test
```

Expected output:
- All tests should pass
- You should see "BUILD SUCCESS" in the output

### Step 4: Run API Tests

First, start the application:

```bash
# Start the application
mvn spring-boot:run
```

Then, in a new terminal window, run the API tests:

```bash
# Make the script executable
chmod +x src/test/achievement-test.sh

# Run the API tests
./src/test/achievement-test.sh
```

Expected output:
- The script should execute all API endpoints successfully
- You should see JSON responses for each endpoint
- The final message should be "Test completed successfully"

### Step 5: Check Test Coverage

```bash
# Generate test coverage report
mvn clean test jacoco:report

# Open the coverage report
open target/site/jacoco/index.html  # On macOS
xdg-open target/site/jacoco/index.html  # On Linux
```

Expected output:
- The coverage report should open in your browser
- Look for high coverage percentages (ideally 80%+) for the achievement-related classes

## 6. Troubleshooting

### Common Issues and Solutions

#### Tests Fail with Compilation Errors

If tests fail with compilation errors, check:
- All required dependencies are in the pom.xml
- The code is properly formatted
- There are no syntax errors

Solution:
```bash
# Clean and rebuild the project
mvn clean compile
```

#### API Tests Fail with Connection Refused

If the API tests fail with "Connection refused", check:
- The application is running
- The application is running on the expected port (default: 8080)
- There are no firewall issues

Solution:
```bash
# Check if the application is running
curl http://localhost:8080/api/achievements

# If not running, start the application
mvn spring-boot:run
```

#### Database-Related Test Failures

If tests fail due to database issues, check:
- The database is running
- The database connection properties are correct
- The database schema is up to date

Solution:
```bash
# Use an in-memory database for testing
# Add this to application-test.properties:
# spring.datasource.url=jdbc:h2:mem:testdb
# spring.datasource.driver-class-name=org.h2.Driver
# spring.jpa.hibernate.ddl-auto=create-drop
```

## 7. Continuous Integration

For continuous integration, you can use GitHub Actions or Jenkins.

### GitHub Actions Example

Create a file `.github/workflows/maven.yml` with the following content:

```yaml
name: Java CI with Maven

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    - name: Set up JDK 11
      uses: actions/setup-java@v2
      with:
        java-version: '11'
        distribution: 'adopt'
    - name: Build with Maven
      run: mvn -B package --file pom.xml
    - name: Test Coverage
      run: mvn jacoco:report
    - name: Upload coverage report
      uses: actions/upload-artifact@v2
      with:
        name: coverage-report
        path: target/site/jacoco/
```

This will run the tests automatically on every push and pull request.
