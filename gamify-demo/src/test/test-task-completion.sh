#!/bin/bash

# Script to test the Task Completion & Points Awarding feature

# Base URL - change this to match your environment
BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Task Completion & Points Awarding Feature Test${NC}"
echo "========================================"

# Step 1: Create a test user
echo -e "\n${YELLOW}Step 1: Creating test user${NC}"

# Create unique username and email using timestamp
timestamp=$(date +%s)
username="taskuser-$timestamp"
email="taskuser-$timestamp@example.com"

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

# Display response
echo -e "\n${YELLOW}Response:${NC}"
echo "$user_response"

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

# Step 2: Send a task completion event
echo -e "\n${YELLOW}Step 2: Sending task completion event${NC}"

# Create a task ID with timestamp to ensure uniqueness
task_id="task-$(date +%s)"

# Create a properly formatted JSON payload for task completion
task_data='{
  "userId": "'$user_id'",
  "taskId": "'$task_id'",
  "event_type": "TASK_COMPLETED",
  "data": {
    "priority": "HIGH",
    "description": "Test task completion"
  }
}'

echo -e "\n${YELLOW}Sending task completion request:${NC}"
echo "$task_data"

# Send the request
task_response=$(curl -s -X POST "$BASE_URL/tasks/events" \
    -H "Content-Type: application/json" \
    -d "$task_data")

# Display response
echo -e "\n${YELLOW}Response:${NC}"
echo "$task_response"

# Check if successful
if echo "$task_response" | grep -q '"success":true'; then
    echo -e "\n${GREEN}✓ Task completion successful${NC}"
    # Extract points awarded
    points=$(echo "$task_response" | grep -o '"pointsAwarded":[0-9]*' | cut -d':' -f2)
    echo -e "${GREEN}Points awarded: $points${NC}"
else
    echo -e "\n${RED}✗ Task completion failed${NC}"
    exit 1
fi

# Step 3: Check user points
echo -e "\n${YELLOW}Step 3: Checking user points${NC}"

# Send the request
points_response=$(curl -s -X GET "$BASE_URL/api/gamification/users/$user_id/points")

# Display response
echo -e "\n${YELLOW}Response:${NC}"
echo "$points_response"

# Extract points
total_points=$(echo "$points_response" | grep -o '"points":[0-9]*' | cut -d':' -f2)

echo -e "\n${GREEN}User has $total_points points${NC}"

echo -e "\n${GREEN}Test completed successfully${NC}"
