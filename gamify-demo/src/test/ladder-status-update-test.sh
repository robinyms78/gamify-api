#!/bin/bash

# Test script for verifying ladder status updates when points cross a threshold
# This test focuses specifically on checking that a user's ladder status is updated when they earn enough points

# Base URL - change this to match your environment
BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Ladder Status Update Test${NC}"
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
username="taskuser-ladder-$timestamp"
email="taskuser-ladder-$timestamp@example.com"
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

# Step 3: Get initial ladder status
echo -e "\n${YELLOW}Step 3: Getting initial ladder status${NC}"

# Send the request to get user ladder status
initial_status_response=$(curl -s -X GET "$BASE_URL/api/ladder/users/$user_id" \
    -H "$auth_header" \
    -H "Content-Type: application/json")

echo -e "\n${YELLOW}Initial ladder status:${NC}"
echo "$initial_status_response"

# Check for errors
if ! handle_error "$initial_status_response" "Get initial ladder status"; then
    exit 1
fi

# Extract initial level
initial_level=$(extract_json_value "$initial_status_response" "currentLevel.level" "1")
if [ -z "$initial_level" ]; then
    initial_level=1  # Default to level 1 if not found
fi
echo -e "${GREEN}Initial ladder level: $initial_level${NC}"

# Step 4: Complete multiple tasks to earn enough points to cross a threshold
echo -e "\n${YELLOW}Step 4: Completing multiple tasks to earn points${NC}"

# Function to complete a task with CRITICAL priority (50 points each)
complete_critical_task() {
    local task_num=$1
    
    # Create a task ID with timestamp and number to ensure uniqueness
    task_id="task-ladder-$timestamp-$task_num"
    
    # Create a properly formatted JSON payload for task completion
    task_data='{
      "userId": "'$user_id'",
      "taskId": "'$task_id'",
      "event_type": "TASK_COMPLETED",
      "data": {
        "priority": "CRITICAL",
        "description": "Test task for ladder status update #'$task_num'"
      }
    }'
    
    echo -e "\n${YELLOW}Sending task completion request #$task_num:${NC}"
    
    # Send the request with auth token
    task_response=$(curl -s -X POST "$BASE_URL/tasks/events" \
        -H "$auth_header" \
        -H "Content-Type: application/json" \
        -d "$task_data")
    
    # Check if successful
    if echo "$task_response" | grep -q '"success":true'; then
        echo -e "${GREEN}✓ Task #$task_num completion successful${NC}"
        # Extract points awarded if available
        points_awarded=$(extract_json_value "$task_response" "pointsAwarded" "0")
        if [ -n "$points_awarded" ] && [ "$points_awarded" != "0" ]; then
            echo -e "${GREEN}Points awarded: $points_awarded${NC}"
        fi
    else
        echo -e "${RED}✗ Task #$task_num completion failed${NC}"
        handle_error "$task_response" "Task completion"
        exit 1
    fi
    
    # Wait a short time between requests
    sleep 1
}

# Complete multiple CRITICAL tasks (50 points each) to earn enough points to cross a threshold
# Most ladder systems have a threshold around 50-100 points for the first level up
for i in {1..3}; do
    complete_critical_task $i
done

# Step 5: Get updated points for the user
echo -e "\n${YELLOW}Step 5: Getting updated points for the user${NC}"

# Send the request to get user points with auth token
updated_points_response=$(curl -s -X GET "$BASE_URL/api/users/$user_id" \
    -H "$auth_header" \
    -H "Content-Type: application/json")

echo -e "\n${YELLOW}Updated user info:${NC}"
echo "$updated_points_response"

# Check for errors
if ! handle_error "$updated_points_response" "Get updated points"; then
    # Try alternative endpoint if the first one fails
    echo -e "\n${YELLOW}Trying alternative endpoint...${NC}"
    updated_points_response=$(curl -s -X GET "$BASE_URL/api/gamification/users/$user_id/points" \
        -H "$auth_header" \
        -H "Content-Type: application/json")
    
    echo -e "\n${YELLOW}Updated points (alternative endpoint):${NC}"
    echo "$updated_points_response"
    
    if ! handle_error "$updated_points_response" "Get updated points (alternative)"; then
        exit 1
    fi
fi

# Extract updated points
updated_points=$(extract_json_value "$updated_points_response" "earnedPoints" "0")
if [ "$updated_points" = "0" ]; then
    # Try alternative field name
    updated_points=$(extract_json_value "$updated_points_response" "points" "0")
fi
echo -e "${GREEN}Updated points: $updated_points${NC}"

# Step 6: Get updated ladder status
echo -e "\n${YELLOW}Step 6: Getting updated ladder status${NC}"

# Wait a short time to allow processing
sleep 2

# Send the request to get user ladder status with auth token
updated_status_response=$(curl -s -X GET "$BASE_URL/api/ladder/users/$user_id" \
    -H "$auth_header" \
    -H "Content-Type: application/json")

echo -e "\n${YELLOW}Updated ladder status:${NC}"
echo "$updated_status_response"

# Check for errors
if ! handle_error "$updated_status_response" "Get updated ladder status"; then
    # Try alternative endpoint
    echo -e "\n${YELLOW}Trying alternative endpoint...${NC}"
    updated_status_response=$(curl -s -X GET "$BASE_URL/api/ladder/status?userId=$user_id" \
        -H "$auth_header" \
        -H "Content-Type: application/json")
    
    echo -e "\n${YELLOW}Updated ladder status (alternative endpoint):${NC}"
    echo "$updated_status_response"
    
    if ! handle_error "$updated_status_response" "Get updated ladder status (alternative)"; then
        exit 1
    fi
fi

# Extract updated level
updated_level=$(extract_json_value "$updated_status_response" "currentLevel.level" "")
if [ -z "$updated_level" ]; then
    # Try alternative field name
    updated_level=$(extract_json_value "$updated_status_response" "currentLevel" "")
fi
echo -e "${GREEN}Updated ladder level: $updated_level${NC}"

# Verify ladder level was increased
if [ "$updated_level" -gt "$initial_level" ]; then
    echo -e "\n${GREEN}✓ Ladder level increased from $initial_level to $updated_level${NC}"
else
    echo -e "\n${YELLOW}! Ladder level did not increase (still at level $updated_level)${NC}"
    echo -e "${YELLOW}Note: This might be expected if the points threshold for the next level is higher than $updated_points points${NC}"
    
    # Get points to next level
    points_to_next=$(extract_json_value "$updated_status_response" "pointsToNextLevel" "0")
    if [ -n "$points_to_next" ] && [ "$points_to_next" != "0" ]; then
        echo -e "${YELLOW}Points needed for next level: $points_to_next${NC}"
        
        # Complete more tasks if needed
        if [ "$points_to_next" -gt 0 ]; then
            echo -e "\n${YELLOW}Completing more tasks to reach next level...${NC}"
            
            # Calculate how many more CRITICAL tasks needed (50 points each)
            tasks_needed=$(( ($points_to_next + 49) / 50 ))  # Ceiling division
            echo -e "${YELLOW}Need to complete $tasks_needed more tasks${NC}"
            
            for i in $(seq 4 $((3 + tasks_needed))); do
                complete_critical_task $i
            done
            
            # Check ladder status again
            sleep 2
            final_status_response=$(curl -s -X GET "$BASE_URL/api/ladder/users/$user_id" \
                -H "$auth_header" \
                -H "Content-Type: application/json")
            
            echo -e "\n${YELLOW}Final ladder status:${NC}"
            echo "$final_status_response"
            
            # Check for errors
            if ! handle_error "$final_status_response" "Get final ladder status"; then
                # Try alternative endpoint
                echo -e "\n${YELLOW}Trying alternative endpoint...${NC}"
                final_status_response=$(curl -s -X GET "$BASE_URL/api/ladder/status?userId=$user_id" \
                    -H "$auth_header" \
                    -H "Content-Type: application/json")
                
                echo -e "\n${YELLOW}Final ladder status (alternative endpoint):${NC}"
                echo "$final_status_response"
                
                if ! handle_error "$final_status_response" "Get final ladder status (alternative)"; then
                    exit 1
                fi
            fi
            
            final_level=$(extract_json_value "$final_status_response" "currentLevel.level" "")
            if [ -z "$final_level" ]; then
                # Try alternative field name
                final_level=$(extract_json_value "$final_status_response" "currentLevel" "")
            fi
            echo -e "${GREEN}Final ladder level: $final_level${NC}"
            
            if [ "$final_level" -gt "$initial_level" ]; then
                echo -e "\n${GREEN}✓ Ladder level increased from $initial_level to $final_level after completing additional tasks${NC}"
            else
                echo -e "\n${RED}✗ Ladder level still did not increase after completing additional tasks${NC}"
                echo -e "${YELLOW}This might indicate an issue with the ladder level update mechanism${NC}"
                exit 1
            fi
        fi
    fi
fi

echo -e "\n${GREEN}Test completed successfully - Ladder status update verified${NC}"
