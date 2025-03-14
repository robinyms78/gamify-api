# Rewards and Redemption Tests

This directory contains tests for the rewards and redemption functionality of the Gamify API.

## Overview

The rewards and redemption tests verify the following functionality:

1. Creating rewards with specific point costs
2. Verifying users cannot redeem rewards without sufficient points
3. Testing the full redemption flow from request to completion
4. Verifying points are correctly deducted after redemption
5. Testing redemption status transitions (PROCESSING → COMPLETED or CANCELLED)
6. Verifying error handling for invalid operations

## Test Scripts

- `rewards-redemption-test.sh`: The main test script that tests the rewards and redemption functionality
- `run-rewards-redemption-test.sh`: A runner script that executes the main test script

## Running the Tests

To run the tests, execute the following command from the `src/test` directory:

```bash
./run-rewards-redemption-test.sh
```

Make sure the application is running on `http://localhost:8080` before executing the tests.

## Test Flow

The test script follows this flow:

1. **Create a test user**: Registers a new user with the system
2. **Award initial points**: Gives the user some points, but not enough for redemption
3. **Create a test reward**: Creates a reward with a cost higher than the user's points
4. **Test insufficient points scenario**: Verifies redemption is rejected when user has insufficient points
5. **Award more points**: Gives the user enough points to redeem the reward
6. **Redeem the reward**: Tests successful redemption and verifies points deduction
7. **Verify redemption record**: Checks that the redemption record has correct properties
8. **Complete the redemption**: Tests the transition from PROCESSING to COMPLETED status
9. **Test cancellation restrictions**: Verifies a completed redemption cannot be cancelled
10. **Test cancellation flow**: Creates another redemption and tests the cancellation process

## Expected Output

When the tests run successfully, you should see output indicating each step was completed successfully, with the final message:

```
All tests completed successfully!
Rewards and redemption functionality is working as expected.
```

If any test fails, the script will exit with a non-zero status code and display an error message indicating which step failed.

## Troubleshooting

If the tests fail, check the following:

1. Ensure the application is running and accessible at `http://localhost:8080`
2. Verify the database is properly set up and accessible
3. Check the application logs for any errors
4. Ensure the user has the necessary permissions to create rewards and redemptions
