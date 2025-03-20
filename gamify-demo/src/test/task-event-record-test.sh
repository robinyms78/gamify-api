#!/bin/bash

# Script to test Task Event Record creation for TASK_COMPLETED events

# Base URL - change this to match your environment
BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Task Event Record Creation Test - TASK_COMPLETED${NC}"
echo "========================================"

# Step 1: Create a test user
echo -e "\n${YELLOW}Step 1: Creating test user${NC}"

# Create unique username and email using timestamp
timestamp=$(date +%s)
username="taskuser-event-record-$timestamp"
email="taskuser-event-record-$timestamp@example.com"

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

# Check if successful
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

# Step 3: Send a task completion event
echo -e "\n${YELLOW}Step 3: Sending task completion event${NC}"

# Create a task ID with timestamp to ensure uniqueness
task_id="task-event-record-$(date +%s)"

# Create a properly formatted JSON payload for task completion
task_data='{
  "userId": "'$user_id'",
  "taskId": "'$task_id'",
  "event_type": "TASK_COMPLETED",
  "data": {
    "priority": "MEDIUM",
    "description": "Test task completion event record",
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

# Check if task completion was successful
if ! echo "$task_response" | grep -q '"success":true'; then
    echo -e "\n${RED}✗ Task completion event sending failed${NC}"
    exit 1
fi

# Step 3: Verify task_events record
echo -e "\n${YELLOW}Step 3: Verify task_events record${NC}"

# Wait for a short period to allow event processing (e.g., 2 seconds)
sleep 2

# Step 3: Verify task_events record using getTaskEventById API
echo -e "\n${YELLOW}Step 3: Verify task_events record using getTaskEventById API${NC}"

# Extract eventId from the task completion response
event_id=$(echo "$task_response" | grep -o '"eventId":"[^"]*"' | cut -d':' -f2 | tr -d '"')

# Query task event by eventId with JWT token
query_response=$(curl -s -X GET "$BASE_URL/tasks/events/$event_id" \
    -H "Authorization: Bearer $jwt_token")

# Display full query response
echo -e "\n${YELLOW}Task Event Details Response:${NC}"
echo "$query_response"

# Check if task event is retrieved successfully and event_type is TASK_COMPLETED
if echo "$query_response" | grep -q '"eventType":"TASK_COMPLETED"'; then
    echo -e "\n${GREEN}✓ Task event record found via getTaskEventById API${NC}"
else
    echo -e "\n${RED}✗ Task event record NOT found via getTaskEventById API${NC}"
    echo -e "\n${RED}✗ Test Failed - No record found for TASK_COMPLETED event using getTaskEventById API${NC}"
    exit 1
fi

echo -e "\n${GREEN}Test completed successfully - Task event record created and verified via getTaskEventById API${NC}"
