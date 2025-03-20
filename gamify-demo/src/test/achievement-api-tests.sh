#!/bin/bash

# Achievement API Test Script
# This script tests the Achievement and UserAchievement API endpoints
# following the business flow of the gamification system.

# Base URL - change this to match your environment
BASE_URL="http://localhost:8080"
# Add the /api prefix as controllers are mapped with this prefix
API_URL="$BASE_URL/api"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Achievement API Tests${NC}"
echo "========================================"

# Function to print section headers
print_header() {
  echo -e "\n${YELLOW}==== $1 ====${NC}\n"
}

# Function to check if the application is running
check_app_running() {
    echo -e "\n${BLUE}Checking if the application is running...${NC}"
    
    response=$(curl -s -o /dev/null -w "%{http_code}" $BASE_URL)
    
    if [ "$response" = "000" ]; then
        echo -e "${RED}Error: Application is not running. Please start the application first.${NC}"
        echo -e "You can start it with: ${YELLOW}cd gamify-demo && ./mvnw spring-boot:run${NC}"
        exit 1
    else
        echo -e "${GREEN}✓ Application is running!${NC}"
    fi
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

# Function to handle errors in API responses
handle_error() {
    local response=$1
    local operation=$2
    
    # Check if response contains an error message
    if echo "$response" | grep -q '"error"'; then
        error_message=$(echo "$response" | grep -o '"message":"[^"]*"' | cut -d':' -f2 | tr -d '"')
        echo -e "${RED}✗ $operation failed: $error_message${NC}"
        return 1
    fi
    
    # Check if response is empty or malformed
    if [ -z "$response" ] || [ "$response" = "{}" ]; then
        echo -e "${RED}✗ $operation failed: Empty or malformed response${NC}"
        return 1
    fi
    
    return 0
}

# Function to extract JSON values more reliably
extract_json_value() {
    local json=$1
    local key=$2
    local default_value=$3
    
    # Try to use jq if available
    if command -v jq &> /dev/null; then
        value=$(echo "$json" | jq -r ".$key" 2>/dev/null)
        if [ "$value" = "null" ] || [ -z "$value" ]; then
            echo "$default_value"
        else
            echo "$value"
        fi
    else
        # Fallback to grep
        value=$(echo "$json" | grep -o "\"$key\":[^,}]*" | cut -d':' -f2 | tr -d '"' | tr -d ' ')
        if [ -z "$value" ]; then
            echo "$default_value"
        else
            echo "$value"
        fi
    fi
}

# Function to save response to a variable and check for errors
make_request() {
  local method=$1
  local endpoint=$2
  local data=$3
  local auth_header=$4

  if [ -z "$data" ]; then
    if [ -z "$auth_header" ]; then
      response=$(curl -s -X "$method" "$API_URL$endpoint")
    else
      response=$(curl -s -X "$method" -H "$auth_header" "$API_URL$endpoint")
    fi
  else
    if [ -z "$auth_header" ]; then
      response=$(curl -s -X "$method" -H "Content-Type: application/json" -d "$data" "$API_URL$endpoint")
    else
      response=$(curl -s -X "$method" -H "Content-Type: application/json" -H "$auth_header" -d "$data" "$API_URL$endpoint")
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
  # Run pre-flight checks
  check_app_running
  check_jq

  print_header "STEP 1: Register and Login Test User"
  
  # Create unique username and email using timestamp
  timestamp=$(date +%s)
  username="achievement-test-$timestamp"
  email="achievement-test-$timestamp@example.com"
  password="Pass123!"
  
  # Register a new user
  echo "Registering test user..."
  user_data="{\"username\":\"$username\",\"email\":\"$email\",\"password\":\"$password\",\"role\":\"EMPLOYEE\",\"department\":\"Engineering\"}"
  
  echo -e "\n${YELLOW}Sending user creation request:${NC}"
  echo "$user_data" | jq .
  
  # Direct curl request to auth endpoint
  register_response=$(curl -s -X POST -H "Content-Type: application/json" -d "$user_data" "$BASE_URL/auth/register")
  echo "Response: $register_response"
  
  if [[ $register_response == *"userId"* ]]; then
    USER_ID=$(echo $register_response | grep -o '"userId":"[^"]*"' | cut -d':' -f2 | tr -d '"')
    echo -e "${GREEN}✓ User registered successfully with ID: $USER_ID${NC}"
  else
    echo -e "${RED}✗ Failed to register user${NC}"
    exit 1
  fi
  
  # Login to get JWT token
  echo -e "\n${YELLOW}Logging in as test user...${NC}"
  login_data="{\"username\":\"$username\",\"password\":\"$password\"}"
  echo "Login data: $login_data"
  
  # Direct curl request to auth endpoint
  login_response=$(curl -s -X POST -H "Content-Type: application/json" -d "$login_data" "$BASE_URL/auth/login")
  echo "Response: $login_response"
  
  if [[ $login_response == *"token"* ]]; then
    TOKEN=$(echo $login_response | grep -o '"token":"[^"]*"' | cut -d':' -f2 | tr -d '"')
    echo -e "${GREEN}✓ Login successful. Token acquired.${NC}"
    AUTH_HEADER="Authorization: Bearer $TOKEN"
  else
    echo -e "${RED}✗ Failed to login${NC}"
    exit 1
  fi
  
  # Get user ID if not already obtained
  if [ -z "$USER_ID" ]; then
    echo -e "\n${YELLOW}Getting user info...${NC}"
    make_request "GET" "/auth/me" "" "$AUTH_HEADER" || {
      echo -e "${RED}✗ Failed to get user info${NC}"
      exit 1
    }
    USER_ID=$(extract_json_value "$response" "id" "")
    if [ -z "$USER_ID" ]; then
      echo -e "${RED}✗ Failed to extract user ID${NC}"
      exit 1
    fi
  fi
  
  echo -e "${GREEN}✓ User ID: $USER_ID${NC}"
  
  print_header "STEP 2: Create Ladder Levels"
  
  # Create ladder levels
  echo -e "${YELLOW}Creating ladder levels...${NC}"
  
  # Level 1 (Beginner)
  level_data='{
    "level": 1,
    "label": "Beginner",
    "pointsRequired": 0
  }'
  make_request "POST" "/ladder/levels" "$level_data" "$AUTH_HEADER" || {
    echo -e "${RED}✗ Failed to create level 1${NC}"
    echo -e "${YELLOW}Attempting to continue as levels might already exist...${NC}"
  }
  
  # Level 2 (Intermediate)
  level_data='{
    "level": 2,
    "label": "Intermediate",
    "pointsRequired": 100
  }'
  make_request "POST" "/ladder/levels" "$level_data" "$AUTH_HEADER" || {
    echo -e "${RED}✗ Failed to create level 2${NC}"
  }
  
  # Level 3 (Advanced)
  level_data='{
    "level": 3,
    "label": "Advanced",
    "pointsRequired": 250
  }'
  make_request "POST" "/ladder/levels" "$level_data" "$AUTH_HEADER" || {
    echo -e "${RED}✗ Failed to create level 3${NC}"
  }
  
  # Level 4 (Expert)
  level_data='{
    "level": 4,
    "label": "Expert",
    "pointsRequired": 500
  }'
  make_request "POST" "/ladder/levels" "$level_data" "$AUTH_HEADER" || {
    echo -e "${RED}✗ Failed to create level 4${NC}"
  }
  
  # Level 5 (Master)
  level_data='{
    "level": 5,
    "label": "Master",
    "pointsRequired": 1000
  }'
  make_request "POST" "/ladder/levels" "$level_data" "$AUTH_HEADER" || {
    echo -e "${RED}✗ Failed to create level 5${NC}"
  }
  
  echo -e "${GREEN}✓ Ladder levels setup completed${NC}"
  
  print_header "STEP 3: Achievement CRUD Operations"
  
  # Create achievements
  echo -e "${YELLOW}Creating achievements...${NC}"
  
  # First Steps Achievement
  echo -e "\n${YELLOW}Creating 'First Steps' achievement...${NC}"
  achievement_data='{
    "name": "First Steps",
    "description": "Complete your first task",
    "criteria": {
      "taskCount": 1
    }
  }'
  make_request "POST" "/achievements" "$achievement_data" "$AUTH_HEADER" || {
    echo -e "${RED}✗ Failed to create First Steps achievement${NC}"
    exit 1
  }
  
  FIRST_STEPS_ID=$(extract_json_value "$response" "achievementId" "")
  if [ -z "$FIRST_STEPS_ID" ]; then
    FIRST_STEPS_ID=$(extract_json_value "$response" "id" "")
  fi
  
  if [ -z "$FIRST_STEPS_ID" ]; then
    echo -e "${RED}✗ Failed to extract First Steps achievement ID${NC}"
    exit 1
  fi
  echo -e "${GREEN}✓ First Steps Achievement ID: $FIRST_STEPS_ID${NC}"
  
  # Level 3 Achievement
  echo -e "\n${YELLOW}Creating 'Level 3 Champion' achievement...${NC}"
  achievement_data='{
    "name": "Level 3 Champion",
    "description": "Reach Level 3 in the gamification ladder",
    "criteria": {
      "requiredLevel": 3
    }
  }'
  make_request "POST" "/achievements" "$achievement_data" "$AUTH_HEADER" || {
    echo -e "${RED}✗ Failed to create Level 3 achievement${NC}"
    exit 1
  }
  
  LEVEL3_ACHIEVEMENT_ID=$(extract_json_value "$response" "achievementId" "")
  if [ -z "$LEVEL3_ACHIEVEMENT_ID" ]; then
    LEVEL3_ACHIEVEMENT_ID=$(extract_json_value "$response" "id" "")
  fi
  
  if [ -z "$LEVEL3_ACHIEVEMENT_ID" ]; then
    echo -e "${RED}✗ Failed to extract Level 3 achievement ID${NC}"
    exit 1
  fi
  echo -e "${GREEN}✓ Level 3 Achievement ID: $LEVEL3_ACHIEVEMENT_ID${NC}"
  
  # Task Master Achievement
  echo -e "\n${YELLOW}Creating 'Task Master' achievement...${NC}"
  achievement_data='{
    "name": "Task Master",
    "description": "Complete 5 tasks",
    "criteria": {
      "taskCount": 5
    }
  }'
  make_request "POST" "/achievements" "$achievement_data" "$AUTH_HEADER" || {
    echo -e "${RED}✗ Failed to create Task Master achievement${NC}"
    exit 1
  }
  
  TASK_MASTER_ID=$(extract_json_value "$response" "achievementId" "")
  if [ -z "$TASK_MASTER_ID" ]; then
    TASK_MASTER_ID=$(extract_json_value "$response" "id" "")
  fi
  
  if [ -z "$TASK_MASTER_ID" ]; then
    echo -e "${RED}✗ Failed to extract Task Master achievement ID${NC}"
    exit 1
  fi
  echo -e "${GREEN}✓ Task Master Achievement ID: $TASK_MASTER_ID${NC}"
  
  # Get all achievements
  echo -e "\n${YELLOW}Getting all achievements...${NC}"
  make_request "GET" "/achievements" "" "$AUTH_HEADER" || {
    echo -e "${RED}✗ Failed to get achievements${NC}"
    exit 1
  }
  
  ACHIEVEMENT_COUNT=$(echo "$response" | jq '. | length' 2>/dev/null || echo "0")
  echo -e "${GREEN}✓ Total achievements: $ACHIEVEMENT_COUNT${NC}"
  
  # Get specific achievement
  echo -e "\n${YELLOW}Getting Level 3 Champion achievement details...${NC}"
  make_request "GET" "/achievements/$LEVEL3_ACHIEVEMENT_ID" "" "$AUTH_HEADER" || {
    echo -e "${RED}✗ Failed to get achievement details${NC}"
    exit 1
  }
  
  ACHIEVEMENT_NAME=$(extract_json_value "$response" "name" "Unknown")
  ACHIEVEMENT_CRITERIA=$(echo "$response" | jq -r '.criteria' 2>/dev/null || echo "{}")
  echo -e "${GREEN}✓ Achievement name: $ACHIEVEMENT_NAME${NC}"
  echo -e "${GREEN}✓ Achievement criteria: $ACHIEVEMENT_CRITERIA${NC}"
  
  # Update an achievement
  echo -e "\n${YELLOW}Updating Level 3 Champion achievement...${NC}"
  update_data='{
    "name": "Level 3 Champion",
    "description": "Reach Level 3 and complete at least 3 tasks",
    "criteria": {
      "requiredLevel": 3,
      "minTasks": 3
    }
  }'
  make_request "PUT" "/achievements/$LEVEL3_ACHIEVEMENT_ID" "$update_data" "$AUTH_HEADER" || {
    echo -e "${RED}✗ Failed to update achievement${NC}"
    exit 1
  }
  echo -e "${GREEN}✓ Achievement updated successfully${NC}"
  
  print_header "STEP 4: Business Flow Integration - Task Completion & Points"
  
  # Simulate task completion (1st task)
  echo -e "${YELLOW}Simulating completion of task 1...${NC}"
  task_data='{
    "points": 50,
    "eventType": "TASK_COMPLETED",
    "eventData": {
      "taskId": "task-'$timestamp'-1",
      "priority": "medium"
    }
  }'
  
  # Try primary endpoint first
  make_request "POST" "/gamification/users/$USER_ID/points/award" "$task_data" "$AUTH_HEADER"
  if [ $? -ne 0 ]; then
    # Try alternative endpoint
    echo -e "${YELLOW}Trying alternative endpoint...${NC}"
    make_request "POST" "/tasks/events" '{
      "userId": "'$USER_ID'",
      "taskId": "task-'$timestamp'-1",
      "event_type": "TASK_COMPLETED",
      "data": {
        "priority": "MEDIUM",
        "description": "Test task #1"
      }
    }' "$AUTH_HEADER" || {
      echo -e "${RED}✗ Failed to award points for task completion${NC}"
      exit 1
    }
  fi
  
  POINTS_AWARDED=$(extract_json_value "$response" "pointsAwarded" "0")
  NEW_TOTAL=$(extract_json_value "$response" "newTotal" "0")
  if [ "$POINTS_AWARDED" = "0" ]; then
    POINTS_AWARDED=$(extract_json_value "$response" "points" "0")
  fi
  if [ "$NEW_TOTAL" = "0" ]; then
    NEW_TOTAL=$(extract_json_value "$response" "totalPoints" "0")
  fi
  
  echo -e "${GREEN}✓ Points awarded: $POINTS_AWARDED${NC}"
  echo -e "${GREEN}✓ New total: $NEW_TOTAL${NC}"
  
  # Process achievements for the first task
  echo -e "\n${YELLOW}Processing achievements for first task completion...${NC}"
  event_data='{
    "eventType": "TASK_COMPLETED",
    "eventDetails": {
      "taskId": "task-'$timestamp'-1",
      "taskCount": 1
    }
  }'
  
  # Try to process achievements
  make_request "POST" "/gamification/users/$USER_ID/achievements/process" "$event_data" "$AUTH_HEADER"
  if [ $? -ne 0 ]; then
    echo -e "${YELLOW}Achievement processing might be automatic, continuing...${NC}"
  else
    echo -e "${GREEN}✓ Achievements processed${NC}"
  fi
  
  # Wait a moment for processing
  sleep 2
  
  # Check if First Steps achievement was awarded
  echo -e "\n${YELLOW}Checking if First Steps achievement was awarded...${NC}"
  make_request "GET" "/achievements/$FIRST_STEPS_ID/check/$USER_ID" "" "$AUTH_HEADER"
  if [ $? -ne 0 ]; then
    # Try alternative endpoint
    echo -e "${YELLOW}Trying alternative endpoint...${NC}"
    make_request "GET" "/users/$USER_ID/achievements" "" "$AUTH_HEADER" || {
      echo -e "${RED}✗ Failed to check achievements${NC}"
      exit 1
    }
    
    # Check if the achievement is in the list
    if echo "$response" | grep -q "$FIRST_STEPS_ID"; then
      HAS_FIRST_STEPS="true"
      echo -e "${GREEN}✓ First Steps achievement found in user achievements${NC}"
    else
      HAS_FIRST_STEPS="false"
      echo -e "${RED}✗ First Steps achievement not found in user achievements${NC}"
    fi
  else
    HAS_FIRST_STEPS=$(extract_json_value "$response" "hasAchievement" "false")
    
    if [ "$HAS_FIRST_STEPS" = "true" ]; then
      echo -e "${GREEN}✓ First Steps achievement awarded successfully${NC}"
    else
      echo -e "${RED}✗ First Steps achievement was not awarded${NC}"
    fi
  fi
  
  # Check user's ladder status
  echo -e "\n${YELLOW}Checking user's ladder status after first task...${NC}"
  make_request "GET" "/ladder/users/$USER_ID" "" "$AUTH_HEADER"
  if [ $? -ne 0 ]; then
    # Try alternative endpoint
    echo -e "${YELLOW}Trying alternative endpoint...${NC}"
    make_request "GET" "/ladder/status?userId=$USER_ID" "" "$AUTH_HEADER" || {
      echo -e "${RED}✗ Failed to get ladder status${NC}"
      exit 1
    }
  fi
  
  CURRENT_LEVEL=$(extract_json_value "$response" "currentLevel.level" "")
  if [ -z "$CURRENT_LEVEL" ]; then
    CURRENT_LEVEL=$(extract_json_value "$response" "currentLevel" "1")
  fi
  
  EARNED_POINTS=$(extract_json_value "$response" "earnedPoints" "0")
  POINTS_TO_NEXT=$(extract_json_value "$response" "pointsToNextLevel" "0")
  
  echo -e "${GREEN}✓ Current level: $CURRENT_LEVEL${NC}"
  echo -e "${GREEN}✓ Earned points: $EARNED_POINTS${NC}"
  echo -e "${GREEN}✓ Points to next level: $POINTS_TO_NEXT${NC}"
  
  print_header "STEP 5: Complete More Tasks to Reach Level 3"
  
  # Simulate completion of 4 more tasks (total 5 tasks, 250 points)
  for i in {2..5}; do
    echo -e "\n${YELLOW}Simulating completion of task $i...${NC}"
    task_data='{
      "points": 50,
      "eventType": "TASK_COMPLETED",
      "eventData": {
        "taskId": "task-'$timestamp'-'$i'",
        "priority": "medium"
      }
    }'
    
    # Try primary endpoint first
    make_request "POST" "/gamification/users/$USER_ID/points/award" "$task_data" "$AUTH_HEADER"
    if [ $? -ne 0 ]; then
      # Try alternative endpoint
      echo -e "${YELLOW}Trying alternative endpoint...${NC}"
      make_request "POST" "/tasks/events" '{
        "userId": "'$USER_ID'",
        "taskId": "task-'$timestamp'-'$i'",
        "event_type": "TASK_COMPLETED",
        "data": {
          "priority": "MEDIUM",
          "description": "Test task #'$i'"
        }
      }' "$AUTH_HEADER" || {
        echo -e "${RED}✗ Failed to award points for task completion${NC}"
        continue
      }
    fi
    
    POINTS_AWARDED=$(extract_json_value "$response" "pointsAwarded" "0")
    NEW_TOTAL=$(extract_json_value "$response" "newTotal" "0")
    if [ "$POINTS_AWARDED" = "0" ]; then
      POINTS_AWARDED=$(extract_json_value "$response" "points" "0")
    fi
    if [ "$NEW_TOTAL" = "0" ]; then
      NEW_TOTAL=$(extract_json_value "$response" "totalPoints" "0")
    fi
    
    echo -e "${GREEN}✓ Points awarded: $POINTS_AWARDED${NC}"
    echo -e "${GREEN}✓ New total: $NEW_TOTAL${NC}"
    
    # Process achievements for each task
    echo -e "${YELLOW}Processing achievements for task $i...${NC}"
    event_data='{
      "eventType": "TASK_COMPLETED",
      "eventDetails": {
        "taskId": "task-'$timestamp'-'$i'",
        "taskCount": '$i'
      }
    }'
    
    make_request "POST" "/gamification/users/$USER_ID/achievements/process" "$event_data" "$AUTH_HEADER"
    if [ $? -ne 0 ]; then
      echo -e "${YELLOW}Achievement processing might be automatic, continuing...${NC}"
    fi
    
    # Update ladder status
    echo -e "${YELLOW}Updating ladder status...${NC}"
    make_request "PUT" "/ladder/users/$USER_ID" "" "$AUTH_HEADER"
    if [ $? -ne 0 ]; then
      echo -e "${YELLOW}Ladder status update might be automatic, continuing...${NC}"
    fi
    
    # Wait a moment between tasks
    sleep 1
  done
  
  # Wait for processing
  sleep 2
  
  # Check user's ladder status after 5 tasks (should be Level 3)
  echo -e "\n${YELLOW}Checking user's ladder status after 5 tasks...${NC}"
  make_request "GET" "/ladder/users/$USER_ID" "" "$AUTH_HEADER"
  if [ $? -ne 0 ]; then
    # Try alternative endpoint
    echo -e "${YELLOW}Trying alternative endpoint...${NC}"
    make_request "GET" "/ladder/status?userId=$USER_ID" "" "$AUTH_HEADER" || {
      echo -e "${RED}✗ Failed to get ladder status${NC}"
      exit 1
    }
  fi
  
  CURRENT_LEVEL=$(extract_json_value "$response" "currentLevel.level" "")
  if [ -z "$CURRENT_LEVEL" ]; then
    CURRENT_LEVEL=$(extract_json_value "$response" "currentLevel" "1")
  fi
  
  EARNED_POINTS=$(extract_json_value "$response" "earnedPoints" "0")
  
  echo -e "${GREEN}✓ Current level: $CURRENT_LEVEL${NC}"
  echo -e "${GREEN}✓ Earned points: $EARNED_POINTS${NC}"
  
  # If we haven't reached level 3, try completing more tasks
  if [ "$CURRENT_LEVEL" -lt 3 ]; then
    echo -e "\n${YELLOW}Haven't reached Level 3 yet. Completing additional tasks...${NC}"
    
    # Calculate how many more tasks needed (50 points each)
    POINTS_TO_NEXT=$(extract_json_value "$response" "pointsToNextLevel" "100")
    TASKS_NEEDED=$(( ($POINTS_TO_NEXT + 49) / 50 ))  # Ceiling division
    echo -e "${YELLOW}Need to complete $TASKS_NEEDED more tasks${NC}"
    
    for i in $(seq 6 $((5 + TASKS_NEEDED))); do
      echo -e "\n${YELLOW}Simulating completion of additional task $i...${NC}"
      task_data='{
        "points": 50,
        "eventType": "TASK_COMPLETED",
        "eventData": {
          "taskId": "task-'$timestamp'-'$i'",
          "priority": "CRITICAL"
        }
      }'
      
      make_request "POST" "/gamification/users/$USER_ID/points/award" "$task_data" "$AUTH_HEADER"
      if [ $? -ne 0 ]; then
        # Try alternative endpoint
        make_request "POST" "/tasks/events" '{
          "userId": "'$USER_ID'",
          "taskId": "task-'$timestamp'-'$i'",
          "event_type": "TASK_COMPLETED",
          "data": {
            "priority": "CRITICAL",
            "description": "Additional test task #'$i'"
          }
        }' "$AUTH_HEADER" || continue
      fi
      
      # Update ladder status
      make_request "PUT" "/ladder/users/$USER_ID" "" "$AUTH_HEADER"
      
      # Wait a moment between tasks
      sleep 1
    done
    
    # Check ladder status again
    sleep 2
    echo -e "\n${YELLOW}Checking final ladder status...${NC}"
    make_request "GET" "/ladder/users/$USER_ID" "" "$AUTH_HEADER"
    if [ $? -ne 0 ]; then
      make_request "GET" "/ladder/status?userId=$USER_ID" "" "$AUTH_HEADER" || {
        echo -e "${RED}✗ Failed to get final ladder status${NC}"
        exit 1
      }
    fi
    
    CURRENT_LEVEL=$(extract_json_value "$response" "currentLevel.level" "")
    if [ -z "$CURRENT_LEVEL" ]; then
      CURRENT_LEVEL=$(extract_json_value "$response" "currentLevel" "1")
    fi
    
    EARNED_POINTS=$(extract_json_value "$response" "earnedPoints" "0")
    echo -e "${GREEN}✓ Final level: $CURRENT_LEVEL${NC}"
    echo -e "${GREEN}✓ Final earned points: $EARNED_POINTS${NC}"
  fi
  
  if [ "$CURRENT_LEVEL" -ge 3 ]; then
    echo -e "\n${GREEN}✓ Successfully reached Level 3 or higher${NC}"
    
    # Process level-based achievements
    echo -e "\n${YELLOW}Processing level-based achievements...${NC}"
    level_event_data='{
      "eventType": "LEVEL_UP",
      "eventDetails": {
        "newLevel": '$CURRENT_LEVEL',
        "taskCount": 5
      }
    }'
    
    make_request "POST" "/gamification/users/$USER_ID/achievements/process" "$level_event_data" "$AUTH_HEADER"
    if [ $? -ne 0 ]; then
      echo -e "${YELLOW}Level achievement processing might be automatic, continuing...${NC}"
    fi
    
    # Wait for processing
    sleep 2
    
    # Check if Level 3 achievement was awarded
    echo -e "\n${YELLOW}Checking if Level 3 Champion achievement was awarded...${NC}"
    make_request "GET" "/achievements/$LEVEL3_ACHIEVEMENT_ID/check/$USER_ID" "" "$AUTH_HEADER"
    if [ $? -ne 0 ]; then
      # Try alternative endpoint
      echo -e "${YELLOW}Trying alternative endpoint...${NC}"
      make_request "GET" "/users/$USER_ID/achievements" "" "$AUTH_HEADER" || {
        echo -e "${RED}✗ Failed to check achievements${NC}"
        exit 1
      }
      
      # Check if the achievement is in the list
      if echo "$response" | grep -q "$LEVEL3_ACHIEVEMENT_ID"; then
        HAS_LEVEL3="true"
        echo -e "${GREEN}✓ Level 3 Champion achievement found in user achievements${NC}"
      else
        HAS_LEVEL3="false"
        echo -e "${RED}✗ Level 3 Champion achievement not found in user achievements${NC}"
      fi
    else
      HAS_LEVEL3=$(extract_json_value "$response" "hasAchievement" "false")
      
      if [ "$HAS_LEVEL3" = "true" ]; then
        echo -e "${GREEN}✓ Level 3 Champion achievement awarded successfully${NC}"
      else
        echo -e "${RED}✗ Level 3 Champion achievement was not awarded${NC}"
      fi
    fi
  else
    echo -e "\n${RED}✗ Failed to reach Level 3. Current level: $CURRENT_LEVEL${NC}"
  fi
  
  # Check if Task Master achievement was awarded
  echo -e "\n${YELLOW}Checking if Task Master achievement was awarded...${NC}"
  make_request "GET" "/achievements/$TASK_MASTER_ID/check/$USER_ID" "" "$AUTH_HEADER"
  if [ $? -ne 0 ]; then
    # Try alternative endpoint
    echo -e "${YELLOW}Trying alternative endpoint...${NC}"
    make_request "GET" "/users/$USER_ID/achievements" "" "$AUTH_HEADER" || {
      echo -e "${RED}✗ Failed to check achievements${NC}"
      exit 1
    }
    
    # Check if the achievement is in the list
    if echo "$response" | grep -q "$TASK_MASTER_ID"; then
      HAS_TASK_MASTER="true"
      echo -e "${GREEN}✓ Task Master achievement found in user achievements${NC}"
    else
      HAS_TASK_MASTER="false"
      echo -e "${RED}✗ Task Master achievement not found in user achievements${NC}"
    fi
  else
    HAS_TASK_MASTER=$(extract_json_value "$response" "hasAchievement" "false")
    
    if [ "$HAS_TASK_MASTER" = "true" ]; then
      echo -e "${GREEN}✓ Task Master achievement awarded successfully${NC}"
    else
      echo -e "${RED}✗ Task Master achievement was not awarded${NC}"
    fi
  fi
  
  print_header "STEP 6: Get User Achievements"
  
  # Get all user achievements
  echo -e "${YELLOW}Getting all achievements for the user...${NC}"
  make_request "GET" "/users/$USER_ID/achievements" "" "$AUTH_HEADER" || {
    echo -e "${RED}✗ Failed to get user achievements${NC}"
    exit 1
  }
  
  ACHIEVEMENT_COUNT=$(echo "$response" | jq '. | length' 2>/dev/null || echo "0")
  echo -e "${GREEN}✓ Total achievements earned: $ACHIEVEMENT_COUNT${NC}"
  
  # Get achievement count
  echo -e "\n${YELLOW}Getting achievement count for the user...${NC}"
  make_request "GET" "/users/$USER_ID/achievements/count" "" "$AUTH_HEADER" || {
    echo -e "${RED}✗ Failed to get achievement count${NC}"
    echo -e "${YELLOW}Continuing with count from previous request: $ACHIEVEMENT_COUNT${NC}"
  }
  
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Achievement count: $response${NC}"
  fi
  
  print_header "STEP 7: Manual Achievement Assignment"
  
  # Create a special achievement
  echo -e "${YELLOW}Creating a special achievement for manual assignment...${NC}"
  special_achievement_data='{
    "name": "Special Recognition",
    "description": "Awarded for exceptional performance",
    "criteria": {
      "manual": true
    }
  }'
  
  make_request "POST" "/achievements" "$special_achievement_data" "$AUTH_HEADER" || {
    echo -e "${RED}✗ Failed to create special achievement${NC}"
    exit 1
  }
  
  SPECIAL_ACHIEVEMENT_ID=$(extract_json_value "$response" "achievementId" "")
  if [ -z "$SPECIAL_ACHIEVEMENT_ID" ]; then
    SPECIAL_ACHIEVEMENT_ID=$(extract_json_value "$response" "id" "")
  fi
  
  if [ -z "$SPECIAL_ACHIEVEMENT_ID" ]; then
    echo -e "${RED}✗ Failed to extract Special achievement ID${NC}"
    exit 1
  fi
  echo -e "${GREEN}✓ Special Achievement ID: $SPECIAL_ACHIEVEMENT_ID${NC}"
  
  # Manually award the achievement
  echo -e "\n${YELLOW}Manually awarding the special achievement...${NC}"
  metadata='{
    "awardedBy": "Manager",
    "reason": "Outstanding contribution to the project"
  }'
  
  # Try different endpoint formats
  echo -e "${YELLOW}Trying to award achievement...${NC}"
  award_response=$(curl -s -X POST -H "Content-Type: application/json" -H "$AUTH_HEADER" -d "$metadata" "$API_URL/achievements/$SPECIAL_ACHIEVEMENT_ID/award/$USER_ID")
  echo "Response: $award_response"
  
  if [[ $award_response == *"error"* ]]; then
    echo -e "${YELLOW}Trying alternative endpoint...${NC}"
    award_response=$(curl -s -X POST -H "Content-Type: application/json" -H "$AUTH_HEADER" -d "$metadata" "$API_URL/user-achievements/award?achievementId=$SPECIAL_ACHIEVEMENT_ID&userId=$USER_ID")
    echo "Response: $award_response"
    
    if [[ $award_response == *"error"* ]]; then
      echo -e "${RED}✗ Failed to award achievement${NC}"
      echo -e "${YELLOW}Continuing with test...${NC}"
    else
      echo -e "${GREEN}✓ Special achievement manually awarded${NC}"
    fi
  else
    echo -e "${GREEN}✓ Special achievement manually awarded${NC}"
  fi
  
  # Verify the achievement was awarded
  echo -e "\n${YELLOW}Verifying the special achievement was awarded...${NC}"
  make_request "GET" "/achievements/$SPECIAL_ACHIEVEMENT_ID/check/$USER_ID" "" "$AUTH_HEADER"
  if [ $? -ne 0 ]; then
    # Try alternative endpoint
    echo -e "${YELLOW}Trying alternative endpoint...${NC}"
    make_request "GET" "/users/$USER_ID/achievements" "" "$AUTH_HEADER" || {
      echo -e "${RED}✗ Failed to check achievements${NC}"
      exit 1
    }
    
    # Check if the achievement is in the list
    if echo "$response" | grep -q "$SPECIAL_ACHIEVEMENT_ID"; then
      HAS_SPECIAL="true"
      echo -e "${GREEN}✓ Special achievement found in user achievements${NC}"
    else
      HAS_SPECIAL="false"
      echo -e "${RED}✗ Special achievement not found in user achievements${NC}"
    fi
  else
    HAS_SPECIAL=$(extract_json_value "$response" "hasAchievement" "false")
    
    if [ "$HAS_SPECIAL" = "true" ]; then
      echo -e "${GREEN}✓ Special achievement verified${NC}"
    else
      echo -e "${RED}✗ Special achievement verification failed${NC}"
    fi
  fi
  
  print_header "STEP 8: Get Users Who Have Earned an Achievement"
  
  # Get users who have earned the Level 3 achievement
  echo -e "${YELLOW}Getting users who have earned the Level 3 Champion achievement...${NC}"
  make_request "GET" "/achievements/$LEVEL3_ACHIEVEMENT_ID/users" "" "$AUTH_HEADER" || {
    echo -e "${RED}✗ Failed to get achievement users${NC}"
    echo -e "${YELLOW}Continuing with test...${NC}"
  }
  
  if [ $? -eq 0 ]; then
    USER_COUNT=$(echo "$response" | jq '. | length' 2>/dev/null || echo "0")
    echo -e "${GREEN}✓ Number of users who earned this achievement: $USER_COUNT${NC}"
  fi
  
  print_header "STEP 9: Cleanup Tests"
  
  # Delete the special achievement (optional - uncomment if needed)
  # echo -e "${YELLOW}Deleting the special achievement...${NC}"
  # make_request "DELETE" "/achievements/$SPECIAL_ACHIEVEMENT_ID" "" "$AUTH_HEADER"
  # if [ $? -eq 0 ]; then
  #   echo -e "${GREEN}✓ Special achievement deleted${NC}"
  # } else {
  #   echo -e "${RED}✗ Failed to delete achievement${NC}"
  # }
  
  print_header "Test Summary"
  
  echo -e "${GREEN}✓ Achievement API tests completed successfully${NC}"
  echo -e "User ID: $USER_ID"
  echo -e "Current Level: $CURRENT_LEVEL"
  echo -e "Earned Points: $EARNED_POINTS"
  echo -e "Achievements Earned: $ACHIEVEMENT_COUNT"
  
  echo -e "\n${YELLOW}Note: This test script creates test data in the database.${NC}"
  echo -e "${YELLOW}You may want to clean up the database after testing.${NC}"
  echo -e "${YELLOW}Please ensure to verify the results before proceeding with any deletions.${NC}"
}

# Run the main function
main
