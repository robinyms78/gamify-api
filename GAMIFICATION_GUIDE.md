# Gamify Demo Application: User Guide

This guide explains the core mechanics of the Gamify Demo application, focusing on how you earn points, progress through ladder levels, and unlock achievements.

## 1. How Points are Calculated for Different Task Types

The Gamify Demo application uses different strategies to calculate points based on the type and characteristics of each task you complete. This ensures a fair and engaging experience.

**Current Point Calculation Strategies:**

*   **Priority-Based Points:** This strategy assigns points based on the priority of the task. Higher priority tasks generally award more points.

    *   **Example:**
        *   Completing a "High" priority task might award 100 points.
        *   Completing a "Medium" priority task might award 50 points.
        *   Completing a "Low" priority task might award 25 points.

    *   **How it works (Technical Detail):** The system uses a `PriorityBasedPointsStrategy` class that implements the `TaskPointsCalculationStrategy` interface.  The `calculatePoints` method within this class determines the points based on the task's priority level (which is likely extracted from the `eventData`).

**Future/Possible Strategies (Illustrative):**

*   **Difficulty-Based Points:**  More difficult tasks would award more points.  A "Hard" task might be worth 150 points, while an "Easy" task might be worth only 30.

*   **Time-Based Points:**  Tasks completed faster might award bonus points.  For example, completing a task within a certain time limit could give you a 20% point bonus.

*   **Custom Points:** Some tasks might have a fixed number of points assigned, regardless of other factors.

## 2. The Ladder Level Progression System

The ladder level system provides a visual representation of your progress and accomplishments within the application. As you earn points, you'll climb through different levels.

**How it Works:**

1.  **Earning Points:** You earn points by completing tasks, as described in the previous section.

2.  **Level Thresholds:** Each ladder level has a specific point threshold. When your total earned points reach or exceed that threshold, you advance to the next level.

3.  **Visual Representation:** Your current level is likely displayed prominently within the application (though the specifics depend on the UI, which I haven't seen).

    *   **Example:**
        *   Level 1: 0 - 499 points
        *   Level 2: 500 - 999 points
        *   Level 3: 1000 - 1999 points
        *   Level 4: 2000 - 3499 points
        *   Level 5: 3500+ points

**Benefits of Leveling Up:**

*   **Recognition:**  Higher levels demonstrate your dedication and skill within the application.
*   **Unlocking Features (Potential):**  Future versions of the application might tie specific features or rewards to reaching certain ladder levels.
*   **Leaderboard Status:** Your level may influence your position on the leaderboard.

## 3. Achievement Types and How They're Unlocked

Achievements are special milestones that recognize specific accomplishments or behaviors within the application. They provide additional goals beyond simply earning points and climbing the ladder.

**Achievement Types (Illustrative Examples):**

*   **Task Completion Achievements:**
    *   **"First Steps":** Awarded for completing your first task.
    *   **"Task Master":** Awarded for completing 100 tasks.
    *   **"High Priority Hero":** Awarded for completing 25 "High" priority tasks.

*   **Points-Based Achievements:**
    *   **"Centurion":** Awarded for earning 100 points.
    *   **"Grandmaster":** Awarded for earning 10,000 points.

*   **Level-Based Achievements:**
    *   **"Level 5 Achiever":** Awarded for reaching Level 5.
    *   **"Top Tier":** Awarded for reaching the highest possible level.

*   **Streak-Based Achievements (Potential):**
    *   **"Daily Doer":** Awarded for completing at least one task every day for a week.
    *   **"Consistent Contributor":** Awarded for completing tasks for 30 consecutive days.

*   **Special Event Achievements (Potential):**
    *   **"Holiday Helper":** Awarded for completing a special task during a holiday event.
    *   **"Anniversary Ace":** Awarded for participating in the application's anniversary celebration.

**How Achievements are Unlocked:**

The application likely tracks your progress on various metrics (tasks completed, points earned, etc.). When you meet the specific criteria for an achievement, it is automatically unlocked and you (likely) receive a notification.

**Benefits of Unlocking Achievements:**

*   **Sense of Accomplishment:** Achievements provide a tangible record of your progress and skill.
*   **Bragging Rights:** You can showcase your unlocked achievements to other users.
*   **Rewards (Potential):** Future versions might offer in-app rewards for unlocking certain achievements.

**Technical Details (Inferred):**

*   Achievement data is likely represented by an `AchievementDTO`.
*   There's probably a service (not shown) that checks for achievement unlock conditions whenever a relevant event (like task completion) occurs.
*   Notifications are likely handled by the `NotificationService`.

---
