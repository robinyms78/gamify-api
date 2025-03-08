#!/bin/bash

# Achievement API Test Script
# This script tests the Achievement and UserAchievement API endpoints
# following the business flow of the gamification system.

# Set the base URL for the API
BASE_URL="http://localhost:8080/api"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print section headers
print_header() {
  echo -e "\n${YELLOW}==== $1 ====${NC}\n"
}

# Function to check if jq is installed
check_jq() {
  if ! command -v jq &> /dev/null; then
    echo -e "${RED}Error: jq is not installed. Please install it to run this script.${NC}"
    echo "On Ubuntu/Debian: sudo apt-get install jq"
    echo "On macOS: brew install jq"
    exit 1
  fi
}

# Function to handle errors
handle_error() {
  echo -e "${RED}Error: $1${NC}"
  exit 1
}

# Function to save response to a variable and check for errors
make_request() {
  local method=$1
  local endpoint=$2
  local data=$3
  local auth_header=$4

  if [ -z "$data" ]; then
    if [ -z "$auth_header" ]; then
      response=$(curl -s -X "$method" "$BASE_URL$endpoint")
    else
      response=$(curl -s -X "$method" -H "$auth_header" "$BASE_URL$endpoint")
    fi
  else
    if [ -z "$auth_header" ]; then
      response=$(curl -s -X "$method" -H "Content-Type: application/json" -d "$data" "$BASE_URL$endpoint")
    else
      response=$(curl -s -X "$method" -H "Content-Type: application/json" -H "$auth_header" -d "$data" "$BASE_URL$endpoint")
    fi
  fi

  # Check if the response is valid JSON
  if ! echo "$response" | jq . > /dev/null 2>&1; then
    echo -e "${RED}Invalid JSON response:${NC}"
    echo "$response"
    return 1
  fi

  # Check for error in response
  if echo "$response" | jq -e 'has("error")' > /dev/null; then
    error_message=$(echo "$response" | jq -r '.message // "Unknown error"')
    echo -e "${RED}API Error: $error_message${NC}"
    return 1
  fi

  return 0
}

# Main test script
main() {
  check_jq

  print_header "STEP 1: Register and Login Test User"
  
  # Register a new user (Sarah)
  echo "Registering user Sarah..."
  user_data='{
    "username": "sarahj",
    "email": "sarah.j@company.com",
    "password": "Pass123!",
    "role": "EMPLOYEE",
    "department": "Engineering"
  }'
  
  make_request "POST" "/auth/register" "$user_data" || handle_error "Failed to register user"
  echo -e "${GREEN}User registered successfully${NC}"
  
  # Login to get JWT token
  echo "Logging in as Sarah..."
  login_data='{
    "username": "sarahj",
    "password": "Pass123!"
  }'
  
  make_request "POST" "/auth/login" "$login_data" || handle_error "Failed to login"
  TOKEN=$(echo "$response" | jq -r '.token')
  
  if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
    handle_error "Failed to extract token from login response"
  fi
  
  echo -e "${GREEN}Login successful. Token acquired.${NC}"
  AUTH_HEADER="Authorization: Bearer $TOKEN"
  
  # Get user ID
  make_request "GET" "/auth/me" "" "$AUTH_HEADER" || handle_error "Failed to get user info"
  USER_ID=$(echo "$response" | jq -r '.id')
  echo "User ID: $USER_ID"
  
  print_header "STEP 2: Create Ladder Levels"
  
  # Create ladder levels
  echo "Creating ladder levels..."
  
  # Level 1 (Beginner)
  level_data='{
    "level": 1,
    "label": "Beginner",
    "pointsRequired": 0
  }'
  make_request "POST" "/ladder/levels" "$level_data" "$AUTH_HEADER" || handle_error "Failed to create level 1"
  
  # Level 2 (Intermediate)
  level_data='{
    "level": 2,
    "label": "Intermediate",
    "pointsRequired": 100
  }'
  make_request "POST" "/ladder/levels" "$level_data" "$AUTH_HEADER" || handle_error "Failed to create level 2"
  
  # Level 3 (Advanced)
  level_data='{
    "level": 3,
    "label": "Advanced",
    "pointsRequired": 250
  }'
  make_request "POST" "/ladder/levels" "$level_data" "$AUTH_HEADER" || handle_error "Failed to create level 3"
  
  # Level 4 (Expert)
  level_data='{
    "level": 4,
    "label": "Expert",
    "pointsRequired": 500
  }'
  make_request "POST" "/ladder/levels" "$level_data" "$AUTH_HEADER" || handle_error "Failed to create level 4"
  
  # Level 5 (Master)
  level_data='{
    "level": 5,
    "label": "Master",
    "pointsRequired": 1000
  }'
  make_request "POST" "/ladder/levels" "$level_data" "$AUTH_HEADER" || handle_error "Failed to create level 5"
  
  echo -e "${GREEN}Ladder levels created successfully${NC}"
  
  print_header "STEP 3: Achievement CRUD Operations"
  
  # Create achievements
  echo "Creating achievements..."
  
  # First Steps Achievement
  achievement_data='{
    "name": "First Steps",
    "description": "Complete your first task",
    "criteria": {
      "taskCount": 1
    }
  }'
  make_request "POST" "/achievements" "$achievement_data" "$AUTH_HEADER" || handle_error "Failed to create First Steps achievement"
  FIRST_STEPS_ID=$(echo "$response" | jq -r '.achievementId')
  echo "First Steps Achievement ID: $FIRST_STEPS_ID"
  
  # Level 3 Achievement
  achievement_data='{
    "name": "Level 3 Champion",
    "description": "Reach Level 3 in the gamification ladder",
    "criteria": {
      "requiredLevel": 3
    }
  }'
  make_request "POST" "/achievements" "$achievement_data" "$AUTH_HEADER" || handle_error "Failed to create Level 3 achievement"
  LEVEL3_ACHIEVEMENT_ID=$(echo "$response" | jq -r '.achievementId')
  echo "Level 3 Achievement ID: $LEVEL3_ACHIEVEMENT_ID"
  
  # Task Master Achievement
  achievement_data='{
    "name": "Task Master",
    "description": "Complete 5 tasks",
    "criteria": {
      "taskCount": 5
    }
  }'
  make_request "POST" "/achievements" "$achievement_data" "$AUTH_HEADER" || handle_error "Failed to create Task Master achievement"
  TASK_MASTER_ID=$(echo "$response" | jq -r '.achievementId')
  echo "Task Master Achievement ID: $TASK_MASTER_ID"
  
  # Get all achievements
  echo "Getting all achievements..."
  make_request "GET" "/achievements" "" "$AUTH_HEADER" || handle_error "Failed to get achievements"
  echo "Total achievements: $(echo "$response" | jq '. | length')"
  
  # Get specific achievement
  echo "Getting Level 3 Champion achievement details..."
  make_request "GET" "/achievements/$LEVEL3_ACHIEVEMENT_ID" "" "$AUTH_HEADER" || handle_error "Failed to get achievement details"
  echo "Achievement name: $(echo "$response" | jq -r '.name')"
  echo "Achievement criteria: $(echo "$response" | jq -r '.criteria')"
  
  # Update an achievement
  echo "Updating Level 3 Champion achievement..."
  update_data='{
    "name": "Level 3 Champion",
    "description": "Reach Level 3 and complete at least 3 tasks",
    "criteria": {
      "requiredLevel": 3,
      "minTasks": 3
    }
  }'
  make_request "PUT" "/achievements/$LEVEL3_ACHIEVEMENT_ID" "$update_data" "$AUTH_HEADER" || handle_error "Failed to update achievement"
  echo -e "${GREEN}Achievement updated successfully${NC}"
  
  print_header "STEP 4: Business Flow Integration - Task Completion & Points"
  
  # Simulate task completion (1st task)
  echo "Simulating completion of task 1..."
  task_data='{
    "points": 50,
    "eventType": "TASK_COMPLETED",
    "eventData": {
      "taskId": "task-1",
      "priority": "medium"
    }
  }'
  make_request "POST" "/gamification/users/$USER_ID/points/award" "$task_data" "$AUTH_HEADER" || handle_error "Failed to award points"
  echo "Points awarded: $(echo "$response" | jq -r '.pointsAwarded')"
  echo "New total: $(echo "$response" | jq -r '.newTotal')"
  
  # Process achievements for the first task
  echo "Processing achievements for first task completion..."
  event_data='{
    "eventType": "TASK_COMPLETED",
    "eventDetails": {
      "taskId": "task-1",
      "taskCount": 1
    }
  }'
  make_request "POST" "/gamification/users/$USER_ID/achievements/process" "$event_data" "$AUTH_HEADER" || handle_error "Failed to process achievements"
  
  # Check if First Steps achievement was awarded
  echo "Checking if First Steps achievement was awarded..."
  make_request "GET" "/achievements/$FIRST_STEPS_ID/check/$USER_ID" "" "$AUTH_HEADER" || handle_error "Failed to check achievement"
  HAS_FIRST_STEPS=$(echo "$response" | jq -r '.hasAchievement')
  
  if [ "$HAS_FIRST_STEPS" = "true" ]; then
    echo -e "${GREEN}First Steps achievement awarded successfully${NC}"
  else
    echo -e "${RED}First Steps achievement was not awarded${NC}"
  fi
  
  # Check user's ladder status
  echo "Checking user's ladder status after first task..."
  make_request "GET" "/ladder/users/$USER_ID" "" "$AUTH_HEADER" || handle_error "Failed to get ladder status"
  echo "Current level: $(echo "$response" | jq -r '.currentLevel')"
  echo "Earned points: $(echo "$response" | jq -r '.earnedPoints')"
  echo "Points to next level: $(echo "$response" | jq -r '.pointsToNextLevel')"
  
  print_header "STEP 5: Complete More Tasks to Reach Level 3"
  
  # Simulate completion of 4 more tasks (total 5 tasks, 250 points)
  for i in {2..5}; do
    echo "Simulating completion of task $i..."
    task_data='{
      "points": 50,
      "eventType": "TASK_COMPLETED",
      "eventData": {
        "taskId": "task-'$i'",
        "priority": "medium"
      }
    }'
    make_request "POST" "/gamification/users/$USER_ID/points/award" "$task_data" "$AUTH_HEADER" || handle_error "Failed to award points"
    echo "Points awarded: $(echo "$response" | jq -r '.pointsAwarded')"
    echo "New total: $(echo "$response" | jq -r '.newTotal')"
    
    # Process achievements for each task
    event_data='{
      "eventType": "TASK_COMPLETED",
      "eventDetails": {
        "taskId": "task-'$i'",
        "taskCount": '$i'
      }
    }'
    make_request "POST" "/gamification/users/$USER_ID/achievements/process" "$event_data" "$AUTH_HEADER" || handle_error "Failed to process achievements"
    
    # Update ladder status
    make_request "PUT" "/ladder/users/$USER_ID" "" "$AUTH_HEADER" || handle_error "Failed to update ladder status"
  done
  
  # Check user's ladder status after 5 tasks (should be Level 3)
  echo "Checking user's ladder status after 5 tasks..."
  make_request "GET" "/ladder/users/$USER_ID" "" "$AUTH_HEADER" || handle_error "Failed to get ladder status"
  CURRENT_LEVEL=$(echo "$response" | jq -r '.currentLevel')
  EARNED_POINTS=$(echo "$response" | jq -r '.earnedPoints')
  
  echo "Current level: $CURRENT_LEVEL"
  echo "Earned points: $EARNED_POINTS"
  
  if [ "$CURRENT_LEVEL" -eq 3 ]; then
    echo -e "${GREEN}Successfully reached Level 3${NC}"
    
    # Process level-based achievements
    echo "Processing level-based achievements..."
    level_event_data='{
      "eventType": "LEVEL_UP",
      "eventDetails": {
        "newLevel": 3,
        "taskCount": 5
      }
    }'
    make_request "POST" "/gamification/users/$USER_ID/achievements/process" "$level_event_data" "$AUTH_HEADER" || handle_error "Failed to process level achievements"
    
    # Check if Level 3 achievement was awarded
    echo "Checking if Level 3 Champion achievement was awarded..."
    make_request "GET" "/achievements/$LEVEL3_ACHIEVEMENT_ID/check/$USER_ID" "" "$AUTH_HEADER" || handle_error "Failed to check achievement"
    HAS_LEVEL3=$(echo "$response" | jq -r '.hasAchievement')
    
    if [ "$HAS_LEVEL3" = "true" ]; then
      echo -e "${GREEN}Level 3 Champion achievement awarded successfully${NC}"
    else
      echo -e "${RED}Level 3 Champion achievement was not awarded${NC}"
    fi
  else
    echo -e "${RED}Failed to reach Level 3. Current level: $CURRENT_LEVEL${NC}"
  fi
  
  # Check if Task Master achievement was awarded
  echo "Checking if Task Master achievement was awarded..."
  make_request "GET" "/achievements/$TASK_MASTER_ID/check/$USER_ID" "" "$AUTH_HEADER" || handle_error "Failed to check achievement"
  HAS_TASK_MASTER=$(echo "$response" | jq -r '.hasAchievement')
  
  if [ "$HAS_TASK_MASTER" = "true" ]; then
    echo -e "${GREEN}Task Master achievement awarded successfully${NC}"
  else
    echo -e "${RED}Task Master achievement was not awarded${NC}"
  fi
  
  print_header "STEP 6: Get User Achievements"
  
  # Get all user achievements
  echo "Getting all achievements for the user..."
  make_request "GET" "/users/$USER_ID/achievements" "" "$AUTH_HEADER" || handle_error "Failed to get user achievements"
  ACHIEVEMENT_COUNT=$(echo "$response" | jq '. | length')
  echo "Total achievements earned: $ACHIEVEMENT_COUNT"
  
  # Get achievement count
  echo "Getting achievement count for the user..."
  make_request "GET" "/users/$USER_ID/achievements/count" "" "$AUTH_HEADER" || handle_error "Failed to get achievement count"
  echo "Achievement count: $response"
  
  print_header "STEP 7: Manual Achievement Assignment"
  
  # Create a special achievement
  echo "Creating a special achievement for manual assignment..."
  special_achievement_data='{
    "name": "Special Recognition",
    "description": "Awarded for exceptional performance",
    "criteria": {
      "manual": true
    }
  }'
  make_request "POST" "/achievements" "$special_achievement_data" "$AUTH_HEADER" || handle_error "Failed to create special achievement"
  SPECIAL_ACHIEVEMENT_ID=$(echo "$response" | jq -r '.achievementId')
  echo "Special Achievement ID: $SPECIAL_ACHIEVEMENT_ID"
  
  # Manually award the achievement
  echo "Manually awarding the special achievement..."
  metadata='{
    "awardedBy": "Manager",
    "reason": "Outstanding contribution to the project"
  }'
  make_request "POST" "/achievements/$SPECIAL_ACHIEVEMENT_ID/award/$USER_ID" "$metadata" "$AUTH_HEADER" || handle_error "Failed to award achievement"
  echo -e "${GREEN}Special achievement manually awarded${NC}"
  
  # Verify the achievement was awarded
  echo "Verifying the special achievement was awarded..."
  make_request "GET" "/achievements/$SPECIAL_ACHIEVEMENT_ID/check/$USER_ID" "" "$AUTH_HEADER" || handle_error "Failed to check achievement"
  HAS_SPECIAL=$(echo "$response" | jq -r '.hasAchievement')
  
  if [ "$HAS_SPECIAL" = "true" ]; then
    echo -e "${GREEN}Special achievement verified${NC}"
  else
    echo -e "${RED}Special achievement verification failed${NC}"
  fi
  
  print_header "STEP 8: Get Users Who Have Earned an Achievement"
  
  # Get users who have earned the Level 3 achievement
  echo "Getting users who have earned the Level 3 Champion achievement..."
  make_request "GET" "/achievements/$LEVEL3_ACHIEVEMENT_ID/users" "" "$AUTH_HEADER" || handle_error "Failed to get achievement users"
  USER_COUNT=$(echo "$response" | jq '. | length')
  echo "Number of users who earned this achievement: $USER_COUNT"
  
  print_header "STEP 9: Cleanup Tests"
  
  # Delete the special achievement (optional - uncomment if needed)
  # echo "Deleting the special achievement..."
  # make_request "DELETE" "/achievements/$SPECIAL_ACHIEVEMENT_ID" "" "$AUTH_HEADER" || handle_error "Failed to delete achievement"
  # echo -e "${GREEN}Special achievement deleted${NC}"
  
  print_header "Test Summary"
  
  echo -e "${GREEN}Achievement API tests completed successfully${NC}"
  echo "User ID: $USER_ID"
  echo "Current Level: $CURRENT_LEVEL"
  echo "Earned Points: $EARNED_POINTS"
  echo "Achievements Earned: $ACHIEVEMENT_COUNT"
  
  echo -e "\n${YELLOW}Note: This test script creates test data in the database.${NC}"
  echo -e "${YELLOW}You may want to clean up the database after testing.${NC}"
}

# Run the main function
main
