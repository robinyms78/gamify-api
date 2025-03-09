# Authentication API Tests

This directory contains a shell script for testing the authentication endpoints based on the user story requirements.

## Prerequisites

- The application must be running on `http://localhost:8080`
- `curl` must be installed on your system
- `jq` must be installed on your system (for pretty-printing JSON responses)

## Running the Tests

1. Make sure the application is running:
   ```bash
   cd gamify-demo
   ./mvnw spring-boot:run
   ```

2. In a separate terminal, make the script executable and run it:
   ```bash
   cd gamify-demo/src/test
   chmod +x auth-api-tests.sh
   ./auth-api-tests.sh
   ```

## What the Tests Cover

The script tests the following scenarios based on the user story:

1. **User Registration**: Tests that a new employee (Sarah) can register with valid details.
2. **Duplicate Username**: Tests that the system returns an appropriate error message when trying to register with a username that already exists.
3. **Duplicate Email**: Tests that the system returns an appropriate error message when trying to register with an email that already exists.
4. **Valid Login**: Tests that Sarah can log in and receive a JWT token and her user details.
5. **Points Initialization**: Verifies that the user's earned_points and available_points are initialized to 0.
6. **Invalid Login**: Tests that the system returns an appropriate error message when trying to log in with invalid credentials.

## User Story Acceptance Criteria

The tests verify the following acceptance criteria from the user story:

1. User is stored in the users table with a unique ID.
2. On logging in via /auth/login, Sarah receives a JWT token and her user details.
3. Appropriate error messages are returned for duplicate registrations or invalid credentials.
4. The system creates a user record with initial earned_points and available_points of 0.

## Interpreting the Results

The script uses color-coded output to indicate success or failure:

- 🟢 Green text indicates a successful test
- 🔴 Red text indicates a failed test
- 🟡 Yellow text is used for section headers

At the end of the script, a summary is displayed showing which user story requirements have been verified.

## Troubleshooting

If the script fails to connect to the application, make sure:

1. The Spring Boot application is running
2. It's running on the default port (8080)
3. There are no firewall issues blocking the connection

If you see errors related to `jq`, make sure it's installed:

- On Ubuntu/Debian: `sudo apt-get install jq`
- On macOS with Homebrew: `brew install jq`
- On Windows with Chocolatey: `choco install jq`
