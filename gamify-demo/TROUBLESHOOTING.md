# Gamify Demo Application: Troubleshooting Guide

This guide helps you troubleshoot common issues that might arise while using or administering the Gamify Demo application.

## 1. Points Calculation Discrepancies

**Scenario:** A user reports that they did not receive the expected number of points for completing a task, or their total points seem incorrect.

**Possible Causes:**

*   **Incorrect Task Data:** The task itself might have been configured with incorrect priority, difficulty, or other attributes that affect points calculation.
*   **Strategy Configuration Error:** The `TaskPointsCalculationStrategy` being used might be misconfigured (e.g., incorrect point values for different priority levels in `PriorityBasedPointsStrategy`).
*   **Logic Error in Strategy:** There might be a bug in the `calculatePoints()` method of the strategy.
*   **Event Handling Failure:** The `TaskCompletedEvent` or `PointsEarnedEvent` might not have been generated or processed correctly. This could be due to a failure in the event publishing mechanism or an error in an event listener.
*   **Database Issue:** There might be a problem with saving the updated points to the database.
*   **Caching Issue (if applicable):** If caching is used, the displayed points might be outdated.

**Investigation Steps:**

1.  **Verify Task Details:** Check the task's configuration (priority, difficulty, etc.) in the system (likely through an admin interface or directly in the database).
2.  **Examine Strategy Configuration:** If using a configuration file or database table to manage strategy parameters, check the settings for the relevant strategy.
3.  **Review Logs:** Examine application logs for any errors related to task completion, points calculation, or event handling. Look for exceptions or warnings.
4.  **Check Event Generation:** If possible, add temporary logging within the code that generates the `TaskCompletedEvent` and `PointsEarnedEvent` to confirm they are being triggered.
5.  **Inspect Event Listeners:** Examine the code of any `@EventListener` methods that handle these events to ensure they are processing the events correctly.
6.  **Database Check:** Directly query the database to check the user's `earnedPoints` in the `users` table and any relevant entries in the `task_events` table (or similar).
7.  **Test with a New Task:** Create a new, simple task with known attributes and see if points are calculated correctly for that task. This can help isolate the issue.

**Resolution Procedures:**

*   **Correct Task Data:** If the task itself was misconfigured, update its attributes.
*   **Adjust Strategy Configuration:** Modify the strategy parameters in the configuration file or database.
*   **Fix Logic Errors:** If there's a bug in the strategy's code, fix the code and redeploy the application.
*   **Address Event Handling Issues:** If events are not being generated or processed correctly, investigate the event publishing mechanism and event listeners. This might involve restarting the application or fixing code errors.
*   **Resolve Database Problems:** If there's a database issue, address the underlying problem (e.g., connectivity, permissions, data integrity).
*   **Clear Cache (if applicable):** If caching is the issue, clear the cache.

## 2. Achievement Unlocking Failures

**Scenario:** A user reports that they met the criteria for an achievement but it was not unlocked.

**Possible Causes:**

*   **Incorrect Achievement Criteria:** The achievement's criteria might be defined incorrectly (e.g., wrong task type, incorrect point threshold).
*   **Logic Error in Achievement Service:** There might be a bug in the code that checks for achievement unlock conditions (likely in an `AchievementService`).
*   **Event Handling Failure:** The relevant event (e.g., `PointsEarnedEvent`, `TaskCompletedEvent`) might not have been generated or processed correctly, preventing the achievement check from triggering.
*   **Database Issue:** There might be a problem with saving the unlocked achievement to the database.

**Investigation Steps:**

1.  **Verify Achievement Criteria:** Check the achievement's definition in the system (likely through an admin interface or directly in the database).
2.  **Review Achievement Service Logic:** Examine the code in the `AchievementService` (or similar) that handles achievement unlocking. Look for any errors in the logic that checks for the criteria.
3.  **Check Event Listeners:** Examine any `@EventListener` methods that are relevant to achievement unlocking (e.g., those listening for `PointsEarnedEvent` or `TaskCompletedEvent`).
4.  **Database Check:** Directly query the database to see if the achievement is recorded as unlocked for the user.
5.  **Test with a Simplified Scenario:** Create a new user and perform the actions required to unlock the achievement, step-by-step, to see if it unlocks correctly. This can help isolate the issue.
6.  **Review Logs:** Examine application logs for any errors related to achievement processing or event handling.

**Resolution Procedures:**

*   **Correct Achievement Criteria:** If the criteria are incorrect, update the achievement definition.
*   **Fix Logic Errors:** If there's a bug in the achievement service, fix the code and redeploy the application.
*   **Address Event Handling Issues:** If events are not being generated or processed correctly, investigate and fix the event handling mechanism.
*   **Resolve Database Problems:** If there's a database issue, address the underlying problem.
*   **Manually Unlock Achievement (if necessary):** As a temporary workaround, you might be able to manually unlock the achievement for the user through an admin interface or by directly modifying the database.

## 3. Ladder Progression Issues

**Scenario:** A user reports that their ladder level is not updating correctly, even though they have earned enough points.

**Possible Causes:**

*   **Incorrect Level Thresholds:** The point thresholds for the ladder levels might be configured incorrectly.
*   **Logic Error in Level Calculation:** There might be a bug in the code that calculates the user's current level based on their points.
*   **Event Handling Failure:** The `PointsEarnedEvent` might not be triggering the level update logic.
*   **Database Issue:** There might be a problem with updating the user's level in the database.

**Investigation Steps:**

1.  **Verify Level Thresholds:** Check the ladder level configuration (likely in the `ladder_levels` table or through an admin interface).
2.  **Review Level Calculation Logic:** Examine the code that calculates the user's level (this might be in a service class or as part of the leaderboard logic).
3.  **Check Event Listeners:** Examine any `@EventListener` methods that handle `PointsEarnedEvent` and are responsible for updating the user's level.
4.  **Database Check:** Directly query the database to check the user's current level and earned points.
5.  **Test with Different Point Values:** Manually adjust the user's points (in a test environment) to see if the level updates correctly at the expected thresholds.

**Resolution Procedures:**

*   **Correct Level Thresholds:** If the thresholds are incorrect, update them in the configuration.
*   **Fix Logic Errors:** If there's a bug in the level calculation logic, fix the code and redeploy.
*   **Address Event Handling Issues:** If events are not triggering the level update, investigate and fix the event handling.
*   **Resolve Database Problems:** If there's a database issue, address the underlying problem.
*   **Manually Update Level (if necessary):** As a temporary workaround, you might be able to manually update the user's level.

## 4. Database Connection Issues

**Scenario:** The application fails to start or experiences database-related errors.

**Possible Causes:**

*   **Incorrect Database Configuration:** The database connection settings in `application.properties` might be incorrect.
*   **Database Server Unavailable:** The PostgreSQL server might be down or unreachable.
*   **SSL Configuration Issues:** The SSL mode might be misconfigured.
*   **Insufficient Permissions:** The database user might not have the necessary permissions.

**Investigation Steps:**

1.  **Check `application.properties`:** Verify the database URL, username, and password settings.
2.  **Test Database Connection:** Use a database client to try connecting to the database with the same credentials.
3.  **Check Database Server Status:** Ensure the PostgreSQL server is running and accessible.
4.  **Review SSL Configuration:** Verify the `spring.datasource.hikari.ssl-mode` setting matches your database's SSL configuration.
5.  **Check Application Logs:** Look for database-related error messages in the application logs.

**Resolution Procedures:**

*   **Correct Configuration:** Update the database settings in `application.properties` if they are incorrect.
*   **Restart Database Server:** If the database server is down, restart it.
*   **Adjust SSL Settings:** Modify the SSL configuration if needed.
*   **Grant Permissions:** Ensure the database user has the necessary permissions.

## 5. JWT Authentication Issues

**Scenario:** Users are unable to authenticate or receive invalid token errors.

**Possible Causes:**

*   **Incorrect JWT Secret:** The `jwt.secret` in `application.properties` might be incorrect or missing.
*   **Token Expiration:** The token might have expired due to a misconfigured `jwt.expiration.ms`.
*   **Clock Skew:** There might be a time difference between the server and client.
*   **Invalid Token Format:** The token might be malformed or corrupted.

**Investigation Steps:**

1.  **Check JWT Configuration:** Verify the `jwt.secret` and `jwt.expiration.ms` settings in `application.properties`.
2.  **Inspect Token:** Use a JWT debugger to examine the token's contents and expiration time.
3.  **Check Server Time:** Ensure the server's clock is synchronized.
4.  **Review Authentication Logs:** Look for authentication-related errors in the application logs.

**Resolution Procedures:**

*   **Update JWT Secret:** Set a valid and secure JWT secret in `application.properties`.
*   **Adjust Token Expiration:** Modify the `jwt.expiration.ms` value if needed.
*   **Synchronize Clocks:** Ensure the server and client clocks are synchronized.
*   **Regenerate Token:** Have the user log in again to generate a new token.

This troubleshooting guide provides a starting point for resolving common issues. The specific steps and solutions will depend on the exact implementation details of your application.
