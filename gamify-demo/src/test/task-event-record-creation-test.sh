#!/bin/bash

# Test script for verifying task event record creation
# This test focuses specifically on checking that a task event record is created with the correct properties

# Base URL - change this to match your environment
BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Task Event Record Creation Test${NC}"
echo "========================================"

# Step 1: Create a test user
echo -e "\n${YELLOW}Step 1: Creating test user${NC}"

# Create unique username and email using timestamp
timestamp=$(date +%s)
username="taskuser-record-$timestamp"
email="taskuser-record-$timestamp@example.com"

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

# Step 2: Send a task completion event
echo -e "\n${YELLOW}Step 2: Sending task completion event${NC}"

# Create a task ID with timestamp to ensure uniqueness
task_id="task-record-$timestamp"

# Create a properly formatted JSON payload for task completion
task_data='{
  "userId": "'$user_id'",
  "taskId": "'$task_id'",
  "event_type": "TASK_COMPLETED",
  "data": {
    "priority": "MEDIUM",
    "description": "Test task event record creation"
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

# Check if successful and extract event ID
if echo "$task_response" | grep -q '"success":true'; then
    echo -e "\n${GREEN}✓ Task completion event sent successfully${NC}"
    # Extract the event ID
    event_id=$(echo "$task_response" | grep -o '"eventId":"[^"]*"' | cut -d':' -f2 | tr -d '"')
    echo -e "${GREEN}Event ID: $event_id${NC}"
else
    echo -e "\n${RED}✗ Task completion event sending failed${NC}"
    exit 1
fi

# Step 3: Verify task event record was created
echo -e "\n${YELLOW}Step 3: Verifying task event record${NC}"

# Wait for a short period to allow event processing
sleep 1

# Query task event by eventId
query_response=$(curl -s -X GET "$BASE_URL/tasks/events/$event_id")

# Display response
echo -e "\n${YELLOW}Task Event Details:${NC}"
echo "$query_response"

# Check if task event is retrieved successfully and has the correct properties
if echo "$query_response" | grep -q '"eventType":"TASK_COMPLETED"'; then
    echo -e "\n${GREEN}✓ Task event record created successfully${NC}"
    
    # Verify user ID
    if echo "$query_response" | grep -q "\"userId\":\"$user_id\""; then
        echo -e "${GREEN}✓ User ID is correct${NC}"
    else
        echo -e "${RED}✗ User ID is incorrect${NC}"
    fi
    
    # Verify task ID
    if echo "$query_response" | grep -q "\"taskId\":\"$task_id\""; then
        echo -e "${GREEN}✓ Task ID is correct${NC}"
    else
        echo -e "${RED}✗ Task ID is incorrect${NC}"
    fi
    
    # Verify event type
    if echo "$query_response" | grep -q '"eventType":"TASK_COMPLETED"'; then
        echo -e "${GREEN}✓ Event type is correct${NC}"
    else
        echo -e "${RED}✗ Event type is incorrect${NC}"
    fi
    
    # Verify status
    if echo "$query_response" | grep -q '"status":"COMPLETED"'; then
        echo -e "${GREEN}✓ Status is correct${NC}"
    else
        echo -e "${RED}✗ Status is incorrect${NC}"
    fi
else
    echo -e "\n${RED}✗ Task event record not found or has incorrect properties${NC}"
    exit 1
fi

echo -e "\n${GREEN}Test completed successfully - Task event record created and verified${NC}"
