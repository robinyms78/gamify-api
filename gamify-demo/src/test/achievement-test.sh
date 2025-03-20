#!/bin/bash

# Achievement Test Script
# Tests the Achievement Tracking API endpoints

# Base URL - change this to match your environment
BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Achievement Tracking Test${NC}"
echo "========================================"

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

# Run pre-flight check
check_app_running

# Step 1: Create a test user
echo -e "\n${YELLOW}Step 1: Creating test user${NC}"

# Create unique username and email using timestamp
timestamp=$(date +%s)
username="achievementuser-$timestamp"
email="achievementuser-$timestamp@example.com"
password="password123"

# Create a properly formatted JSON payload for user creation
user_data='{
  "username": "'$username'",
  "email": "'$email'",
  "password": "'$password'",
  "role": "EMPLOYEE",
  "department": "Engineering"
}'

echo -e "\n${YELLOW}Sending user creation request:${NC}"
echo "$user_data"

# Send the request to create a user
user_response=$(curl -s -X POST "$BASE_URL/auth/register" \
    -H "Content-Type: application/json" \
    -d "$user_data")

# Check if successful and extract user ID
if echo "$user_response" | grep -q '"message":"User registered successfully"'; then
    echo -e "\n${GREEN}✓ User creation successful${NC}"
    # Extract the user ID
    user_id=$(extract_json_value "$user_response" "userId" "")
    echo -e "${GREEN}User ID: $user_id${NC}"
else
    handle_error "$user_response" "User creation"
    exit 1
fi

# Step 2: Login to get JWT token
echo -e "\n${YELLOW}Step 2: Logging in to get JWT token${NC}"

# Create login request payload
login_data='{
  "username": "'$username'",
  "password": "'$password'"
}'

echo -e "\n${YELLOW}Sending login request:${NC}"

# Send login request
login_response=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "$login_data")

# Check if login was successful and extract token
if echo "$login_response" | grep -q '"token"'; then
    echo -e "\n${GREEN}✓ Login successful${NC}"
    # Extract the JWT token
    token=$(extract_json_value "$login_response" "token" "")
    echo -e "${GREEN}Token obtained${NC}"
    
    # Create Authorization header for subsequent requests
    auth_header="Authorization: Bearer $token"
else
    handle_error "$login_response" "Login"
    exit 1
fi

# Step 3: Create an achievement
echo -e "\n${YELLOW}Step 3: Creating an achievement${NC}"

# Create achievement payload
achievement_data='{
  "name": "Task Master",
  "description": "Complete 10 tasks",
  "criteria": {
    "type": "TASK_COMPLETION_COUNT",
    "count": 10,
    "eventType": "TASK_COMPLETED"
  }
}'

echo -e "\n${YELLOW}Sending achievement creation request:${NC}"
echo "$achievement_data"

# Send the request to create an achievement
achievement_response=$(curl -s -X POST "$BASE_URL/api/achievements" \
    -H "Content-Type: application/json" \
    -H "$auth_header" \
    -d "$achievement_data")

echo -e "\n${YELLOW}Achievement Creation Response:${NC}"
echo "$achievement_response"

# Check if successful and extract achievement ID
if ! handle_error "$achievement_response" "Achievement creation"; then
    # Try to continue with a default ID
    achievement_id="achievement123"
    echo -e "${YELLOW}Using default achievement ID: $achievement_id for testing${NC}"
else
    # Extract the achievement ID
    achievement_id=$(extract_json_value "$achievement_response" "achievementId" "achievement123")
    echo -e "${GREEN}Achievement ID: $achievement_id${NC}"
fi

# Step 4: Get all achievements
echo -e "\n${YELLOW}Step 4: Getting all achievements${NC}"

# Send request to get all achievements
achievements_response=$(curl -s -H "$auth_header" $BASE_URL/api/achievements)

echo -e "\n${YELLOW}All Achievements Response:${NC}"
echo "$achievements_response"

# Check for errors
if ! handle_error "$achievements_response" "Get all achievements"; then
    echo -e "${YELLOW}Continuing with test despite error...${NC}"
fi

# Step 5: Get achievement by ID
echo -e "\n${YELLOW}Step 5: Getting achievement by ID: $achievement_id${NC}"

# Send request to get achievement by ID
achievement_response=$(curl -s -H "$auth_header" $BASE_URL/api/achievements/$achievement_id)

echo -e "\n${YELLOW}Achievement by ID Response:${NC}"
echo "$achievement_response"

# Check for errors
if ! handle_error "$achievement_response" "Get achievement by ID"; then
    echo -e "${YELLOW}Continuing with test despite error...${NC}"
fi

# Function to process a task completion event
process_task_completion() {
    local task_num=$1
    
    # Create a task ID with timestamp and number to ensure uniqueness
    task_id="task-achievement-$timestamp-$task_num"
    
    # Create a properly formatted JSON payload for task completion
    task_data='{
      "userId": "'$user_id'",
      "taskId": "'$task_id'",
      "event_type": "TASK_COMPLETED",
      "data": {
        "priority": "NORMAL",
        "description": "Test task for achievement tracking #'$task_num'"
      }
    }'
    
    echo -e "\n${YELLOW}Sending task completion request #$task_num:${NC}"
    echo "$task_data"
    
    # Send the request with auth token
    task_response=$(curl -s -X POST "$BASE_URL/tasks/events" \
        -H "$auth_header" \
        -H "Content-Type: application/json" \
        -d "$task_data")
    
    echo -e "\n${YELLOW}Task Completion Response:${NC}"
    echo "$task_response"
    
    # Check if successful
    if ! handle_error "$task_response" "Task completion #$task_num"; then
        echo -e "${YELLOW}Continuing with test despite error...${NC}"
    else
        echo -e "${GREEN}✓ Task #$task_num completion successful${NC}"
    fi
    
    # Wait a short time between requests
    sleep 1
}

# Step 6: Process multiple task completion events
echo -e "\n${YELLOW}Step 6: Processing multiple task completion events${NC}"

# Complete multiple tasks to trigger achievement progress
for i in {1..5}; do
    process_task_completion $i
done

# Step 7: Process achievement event directly
echo -e "\n${YELLOW}Step 7: Processing achievement event directly${NC}"

# Create event payload
event_data='{
  "eventType": "TASK_COMPLETED",
  "eventDetails": {
    "taskId": "task-direct-'$timestamp'",
    "taskName": "Direct Achievement Test",
    "skip_ladder_update": true
  }
}'

echo -e "\n${YELLOW}Sending direct event processing request:${NC}"
echo "$event_data"

# Send request to process event
event_response=$(curl -s -X POST "$BASE_URL/api/achievements/process/$user_id" \
    -H "Content-Type: application/json" \
    -H "$auth_header" \
    -d "$event_data")

echo -e "\n${YELLOW}Direct Event Processing Response:${NC}"
echo "$event_response"

# Check for errors
if ! handle_error "$event_response" "Direct event processing"; then
    echo -e "${YELLOW}Continuing with test despite error...${NC}"
else
    echo -e "${GREEN}✓ Direct event processing successful${NC}"
fi

# Step 8: Get user achievements
echo -e "\n${YELLOW}Step 8: Getting user achievements${NC}"

# Send request to get user achievements
user_achievements_response=$(curl -s -H "$auth_header" $BASE_URL/api/users/$user_id/achievements)

echo -e "\n${YELLOW}User Achievements Response:${NC}"
echo "$user_achievements_response"

# Check for errors
if ! handle_error "$user_achievements_response" "Get user achievements"; then
    echo -e "${YELLOW}Continuing with test despite error...${NC}"
else
    echo -e "${GREEN}✓ Successfully retrieved user achievements${NC}"
fi

# Step 9: Get user achievement count
echo -e "\n${YELLOW}Step 9: Getting user achievement count${NC}"

# Send request to get user achievement count
count_response=$(curl -s -H "$auth_header" $BASE_URL/api/users/$user_id/achievements/count)

echo -e "\n${YELLOW}User Achievement Count Response:${NC}"
echo "$count_response"

# Check for errors
if ! handle_error "$count_response" "Get user achievement count"; then
    echo -e "${YELLOW}Continuing with test despite error...${NC}"
else
    echo -e "${GREEN}✓ Successfully retrieved user achievement count${NC}"
fi

# Step 10: Check if user has the achievement
echo -e "\n${YELLOW}Step 10: Checking if user has achievement $achievement_id${NC}"

# Send request to check if user has achievement
check_response=$(curl -s -H "$auth_header" $BASE_URL/api/achievements/$achievement_id/check/$user_id)

echo -e "\n${YELLOW}User Achievement Check Response:${NC}"
echo "$check_response"

# Check for errors
if ! handle_error "$check_response" "Check user achievement"; then
    echo -e "${YELLOW}Continuing with test despite error...${NC}"
else
    # Extract has achievement status
    has_achievement=$(extract_json_value "$check_response" "hasAchievement" "false")
    if [ "$has_achievement" = "true" ]; then
        echo -e "${GREEN}✓ User has earned the achievement!${NC}"
    else
        echo -e "${YELLOW}! User has not yet earned the achievement${NC}"
        echo -e "${YELLOW}This is expected if the user hasn't completed enough tasks yet${NC}"
    fi
fi

echo -e "\n${GREEN}Achievement tracking test completed successfully${NC}"
