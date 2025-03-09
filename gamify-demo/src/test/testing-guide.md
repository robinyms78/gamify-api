# Testing Guide for Ladder Status Feature

This guide will walk you through the process of testing the ladder status feature using different testing approaches.

## 1. Running Unit Tests

The unit tests verify that individual components of the ladder status feature work correctly in isolation.

### Running Service Tests

To run the `LadderStatusServiceImplTest`:

```bash
cd gamify-demo
mvn test -Dtest=LadderStatusServiceImplTest
```

This test verifies:
- Getting ladder status for existing users
- Handling non-existent users
- Initializing ladder status for new users
- Updating ladder status when users level up

### Running Controller Tests

To run the `LadderControllerTest`:

```bash
cd gamify-demo
mvn test -Dtest=LadderControllerTest
```

This test verifies:
- The `/api/ladder/status` endpoint returns the correct response
- Error handling for non-existent users
- The endpoint correctly processes query parameters

## 2. Running Integration Tests

The integration tests verify that all components of the ladder status feature work correctly together.

To run the `LadderStatusIntegrationTest`:

```bash
cd gamify-demo
mvn test -Dtest=LadderStatusIntegrationTest
```

This test verifies:
- The entire flow from controller to service to repository
- Database interactions
- Level-up functionality

## 3. Manual Testing

### Prerequisites

1. Make sure the application is running:

```bash
cd gamify-demo
mvn spring-boot:run
```

2. Create a test user (if you don't have one already):

```bash
cd gamify-demo
./src/test/create-test-user.sh
```

Note the user ID from the response.

### Using the Shell Script

1. Edit the `ladder-status-test.sh` script to use your test user ID:

```bash
cd gamify-demo
nano src/test/ladder-status-test.sh
```

Update the `USER_ID` variable with your test user's ID:

```bash
USER_ID="your-test-user-id"  # Replace with an actual user ID from your database
```

2. Run the test script:

```bash
cd gamify-demo
./src/test/ladder-status-test.sh
```

The script will:
- Test getting ladder status for your user
- Test error handling for non-existent users
- Display the responses in a formatted way

### Manual API Testing with curl

You can also test the API manually using curl:

1. Get ladder status for a user:

```bash
curl -X GET "http://localhost:8080/api/ladder/status?userId=your-test-user-id"
```

2. Test with a non-existent user:

```bash
curl -X GET "http://localhost:8080/api/ladder/status?userId=non-existent-user"
```

## 4. Testing Level-Up Functionality

To test the level-up functionality:

1. Check your user's current ladder status:

```bash
curl -X GET "http://localhost:8080/api/ladder/status?userId=your-test-user-id"
```

Note the current level and points to next level.

2. Update your user's points to exceed the threshold for the next level:

```sql
-- Run this SQL in your database
UPDATE users SET earned_points = 250 WHERE id = 'your-test-user-id';
```

3. Get the updated ladder status:

```bash
curl -X GET "http://localhost:8080/api/ladder/status?userId=your-test-user-id"
```

You should see that the user has leveled up.

## 5. Troubleshooting

If you encounter issues during testing:

1. Check the application logs for errors:

```bash
cd gamify-demo
tail -f logs/application.log
```

2. Verify that the database contains the necessary data:

```sql
-- Check ladder levels
SELECT * FROM ladder_levels;

-- Check user ladder status
SELECT * FROM user_ladder_status WHERE user_id = 'your-test-user-id';

-- Check user points
SELECT id, earned_points FROM users WHERE id = 'your-test-user-id';
```

3. Ensure the application is running and accessible:

```bash
curl -X GET "http://localhost:8080/api/health"
```

This should return a 200 OK response.
