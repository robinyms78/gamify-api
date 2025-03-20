#!/bin/bash

# Test script for verifying points calculation for different task priorities
# This test focuses specifically on checking that the correct number of points are awarded based on task priority

# Base URL - change this to match your environment
BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Points Calculation Test for Different Task Priorities${NC}"
echo "========================================"

# Step 1: Create a test user
echo -e "\n${YELLOW}Step 1: Creating test user${NC}"

# Create unique username and email using timestamp
timestamp=$(date +%s)
username="taskuser-points-$timestamp"
email="taskuser-points-$timestamp@example.com"

# Create a properly formatted JSON payload for user creation
user_data='{
  "username": "'$username'",
  "email": "'$email'",
  "password": "password123",
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
    user_id=$(echo "$user_response" | grep -o '"userId":"[^"]*"' | cut -d':' -f2 | tr -d '"')
    echo -e "${GREEN}User ID: $user_id${NC}"
else
    echo -e "\n${RED}✗ User creation failed${NC}"
    exit 1
fi

# Step 2: Login to get JWT token
echo -e "\n${YELLOW}Step 2: Logging in to get JWT token${NC}"

# Create login payload
login_data='{
  "username": "'$username'",
  "password": "password123"
}'

echo -e "\n${YELLOW}Sending login request:${NC}"
echo "$login_data"

# Send login request
login_response=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "$login_data")

echo -e "\n${YELLOW}Login Response:${NC}"
echo "$login_response"

# Extract JWT token
if echo "$login_response" | grep -q '"token"'; then
    echo -e "\n${GREEN}✓ Login successful${NC}"
    # Extract the JWT token
    jwt_token=$(echo "$login_response" | grep -o '"token":"[^"]*"' | cut -d':' -f2 | tr -d '"')
    echo -e "${GREEN}JWT Token: $jwt_token${NC}"
else
    echo -e "\n${RED}✗ Login failed${NC}"
    exit 1
fi

# Function to test task completion with a specific priority
test_priority() {
    local priority=$1
    local expected_points=$2
    
    echo -e "\n${YELLOW}Testing $priority priority task:${NC}"
    
    # Create a task ID with timestamp and priority to ensure uniqueness
    task_id="task-$priority-$timestamp"
    
    # Create a properly formatted JSON payload for task completion
    task_data='{
      "userId": "'$user_id'",
      "taskId": "'$task_id'",
      "event_type": "TASK_COMPLETED",
      "data": {
        "priority": "'$priority'",
        "description": "Test task with '$priority' priority",
        "skip_ladder_update": true
      }
    }'
    
    echo -e "\n${YELLOW}Sending task completion request:${NC}"
    echo "$task_data"
    
    # Send the request with JWT token
    task_response=$(curl -s -X POST "$BASE_URL/tasks/events" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $jwt_token" \
        -d "$task_data")
    
    # Display response
    echo -e "\n${YELLOW}Response:${NC}"
    echo "$task_response"
    
    # Check if successful and verify points awarded
    if echo "$task_response" | grep -q '"success":true'; then
        echo -e "\n${GREEN}✓ Task completion event sent successfully${NC}"
        
        # Extract points awarded
        points_awarded=$(echo "$task_response" | grep -o '"pointsAwarded":[0-9]*' | cut -d':' -f2)
        
        # Verify points awarded match expected points
        if [ "$points_awarded" -eq "$expected_points" ]; then
            echo -e "${GREEN}✓ Points awarded ($points_awarded) match expected points for $priority priority ($expected_points)${NC}"
        else
            echo -e "${RED}✗ Points awarded ($points_awarded) do not match expected points for $priority priority ($expected_points)${NC}"
            exit 1
        fi
    else
        echo -e "\n${RED}✗ Task completion event sending failed${NC}"
        exit 1
    fi
    
    # Wait a short time between requests
    sleep 1
}

# Step 2: Test LOW priority task (10 points)
test_priority "LOW" 10

# Step 3: Test MEDIUM priority task (20 points)
test_priority "MEDIUM" 20

# Step 4: Test HIGH priority task (30 points)
test_priority "HIGH" 30

# Step 5: Test CRITICAL priority task (50 points)
test_priority "CRITICAL" 50

# Step 6: Test DEFAULT priority (no priority specified) (15 points)
echo -e "\n${YELLOW}Testing DEFAULT priority task (no priority specified):${NC}"

# Create a task ID with timestamp and priority to ensure uniqueness
task_id="task-DEFAULT-$timestamp"

# Create a properly formatted JSON payload for task completion without priority
task_data='{
  "userId": "'$user_id'",
  "taskId": "'$task_id'",
  "event_type": "TASK_COMPLETED",
  "data": {
    "description": "Test task with no priority specified",
    "skip_ladder_update": true
  }
}'

echo -e "\n${YELLOW}Sending task completion request:${NC}"
echo "$task_data"

# Send the request with JWT token
task_response=$(curl -s -X POST "$BASE_URL/tasks/events" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $jwt_token" \
    -d "$task_data")

# Display response
echo -e "\n${YELLOW}Response:${NC}"
echo "$task_response"

# Check if successful and verify points awarded
if echo "$task_response" | grep -q '"success":true'; then
    echo -e "\n${GREEN}✓ Task completion event sent successfully${NC}"
    
    # Extract points awarded
    points_awarded=$(echo "$task_response" | grep -o '"pointsAwarded":[0-9]*' | cut -d':' -f2)
    
    # Verify points awarded match expected points for DEFAULT priority (15)
    if [ "$points_awarded" -eq 15 ]; then
        echo -e "${GREEN}✓ Points awarded ($points_awarded) match expected points for DEFAULT priority (15)${NC}"
    else
        echo -e "${RED}✗ Points awarded ($points_awarded) do not match expected points for DEFAULT priority (15)${NC}"
        exit 1
    fi
else
    echo -e "\n${RED}✗ Task completion event sending failed${NC}"
    exit 1
fi

echo -e "\n${GREEN}Test completed successfully - Points calculation verified for all priority levels${NC}"
