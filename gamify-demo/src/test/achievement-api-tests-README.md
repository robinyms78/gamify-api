# Achievement API Tests

This directory contains test scripts for the Achievement and UserAchievement API endpoints in the Gamification system. These tests follow the business flow of the gamification system, from user registration to achievement unlocking and ladder progression.

## Test Files

1. **achievement-api-tests.sh**: Bash script for testing the Achievement API endpoints using curl commands.
2. **achievement-api-tests.postman_collection.json**: Postman collection for testing the Achievement API endpoints.
3. **achievement-api-tests.postman_environment.json**: Postman environment variables for the collection.

## Prerequisites

- The Gamification API server must be running on `http://localhost:8080`.
- For the bash script:
  - Bash shell
  - curl
  - jq (for JSON processing)
- For the Postman collection:
  - Postman application

## Running the Bash Script

1. Make the script executable:
   ```bash
   chmod +x achievement-api-tests.sh
   ```

2. Run the script:
   ```bash
   ./achievement-api-tests.sh
   ```

The script will:
1. Register a test user (Sarah)
2. Create ladder levels
3. Create achievements
4. Simulate task completion
5. Process achievements
6. Check ladder progression
7. Verify achievement unlocking
8. Test manual achievement assignment

## Using the Postman Collection

1. Import the collection and environment files into Postman:
   - File > Import > Upload Files
   - Select both `achievement-api-tests.postman_collection.json` and `achievement-api-tests.postman_environment.json`

2. Select the "Gamification API Environment" from the environment dropdown in Postman.

3. Run the collection:
   - Click on the "Achievement API Tests" collection
   - Click the "Run" button
   - Select the requests you want to run
   - Click "Run Achievement API Tests"

Alternatively, you can run the requests individually in the order they appear in the collection.

## Test Flow

The tests follow this business flow:

1. **Authentication**: Register and login a test user.
2. **Ladder Setup**: Create ladder levels with different point thresholds.
3. **Achievement CRUD**: Create, read, update, and delete achievements.
4. **Business Flow Integration**: Simulate task completion and points awarding.
5. **Level Progression**: Complete tasks to reach Level 3.
6. **User Achievements**: Get user achievements and counts.
7. **Manual Achievement Assignment**: Manually award a special achievement.
8. **Achievement Users**: Get users who have earned a specific achievement.
9. **Cleanup**: Delete test data (optional).

## Notes

- The tests create data in the database. You may want to clean up the database after testing.
- The tests assume a fresh database or at least no conflicts with existing data (e.g., no user with the same username).
- The Postman collection includes test scripts that automatically save response data to environment variables.
- The bash script includes error handling and colorized output for better readability.

## Customization

- To change the API base URL:
  - For the bash script: Edit the `BASE_URL` variable at the top of the script.
  - For Postman: Edit the `baseUrl` environment variable.

- To change the test user details:
  - Edit the user registration payload in both the bash script and Postman collection.
