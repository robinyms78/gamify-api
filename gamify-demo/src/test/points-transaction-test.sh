#!/bin/bash

# Test script for verifying points transaction record creation
# This test focuses specifically on checking that a points transaction record is created with the correct properties

# Base URL - change this to match your environment
BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Points Transaction Record Creation Test${NC}"
echo "========================================"

# Step 1: Create a test user
echo -e "\n${YELLOW}Step 1: Creating test user${NC}"

# Create unique username and email using timestamp
timestamp=$(date +%s)
username="taskuser-transaction-$timestamp"
email="taskuser-transaction-$timestamp@example.com"

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

# Step 2: Get initial points for the user
echo -e "\n${YELLOW}Step 2: Getting initial points for the user${NC}"

# Send the request to get user points
initial_points_response=$(curl -s -X GET "$BASE_URL/api/gamification/users/$user_id/points")

echo -e "\n${YELLOW}Initial points:${NC}"
echo "$initial_points_response"

# Extract initial points (should be 0 for a new user)
initial_points=$(echo "$initial_points_response" | grep -o '"points":[0-9]*' | cut -d':' -f2)
if [ -z "$initial_points" ]; then
    initial_points=0
fi
echo -e "${GREEN}Initial points: $initial_points${NC}"

# Step 3: Send a task completion event
echo -e "\n${YELLOW}Step 3: Sending task completion event${NC}"

# Create a task ID with timestamp to ensure uniqueness
task_id="task-transaction-$timestamp"

# Create a properly formatted JSON payload for task completion
task_data='{
  "userId": "'$user_id'",
  "taskId": "'$task_id'",
  "event_type": "TASK_COMPLETED",
  "data": {
    "priority": "HIGH",
    "description": "Test task for transaction record"
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

# Check if successful and extract points awarded
if echo "$task_response" | grep -q '"success":true'; then
    echo -e "\n${GREEN}✓ Task completion event sent successfully${NC}"
    # Extract points awarded
    points_awarded=$(echo "$task_response" | grep -o '"pointsAwarded":[0-9]*' | cut -d':' -f2)
    echo -e "${GREEN}Points awarded: $points_awarded${NC}"
else
    echo -e "\n${RED}✗ Task completion event sending failed${NC}"
    exit 1
fi

# Step 4: Get updated points for the user
echo -e "\n${YELLOW}Step 4: Getting updated points for the user${NC}"

# Wait a short time to allow processing
sleep 1

# Send the request to get user points
updated_points_response=$(curl -s -X GET "$BASE_URL/api/gamification/users/$user_id/points")

echo -e "\n${YELLOW}Updated points:${NC}"
echo "$updated_points_response"

# Extract updated points
updated_points=$(echo "$updated_points_response" | grep -o '"points":[0-9]*' | cut -d':' -f2)
echo -e "${GREEN}Updated points: $updated_points${NC}"

# Verify points were increased by the expected amount
expected_points=$((initial_points + points_awarded))
if [ "$updated_points" -eq "$expected_points" ]; then
    echo -e "${GREEN}✓ Points increased by the expected amount ($points_awarded)${NC}"
else
    echo -e "${RED}✗ Points did not increase by the expected amount${NC}"
    echo -e "${RED}  Expected: $expected_points, Actual: $updated_points${NC}"
    exit 1
fi

# Step 5: Verify points transaction record
echo -e "\n${YELLOW}Step 5: Verifying points transaction record${NC}"

# Send the request to get user transactions
transactions_response=$(curl -s -X GET "$BASE_URL/api/gamification/users/$user_id/transactions")

echo -e "\n${YELLOW}Transactions:${NC}"
echo "$transactions_response"

# Check if a transaction exists with the correct properties
if echo "$transactions_response" | grep -q "\"eventType\":\"TASK_COMPLETED\""; then
    echo -e "\n${GREEN}✓ Transaction record found with event type TASK_COMPLETED${NC}"
    
    # Verify points amount
    if echo "$transactions_response" | grep -q "\"points\":$points_awarded"; then
        echo -e "${GREEN}✓ Transaction has correct points amount ($points_awarded)${NC}"
    else
        echo -e "${RED}✗ Transaction has incorrect points amount${NC}"
        exit 1
    fi
    
    # Verify task ID
    if echo "$transactions_response" | grep -q "\"taskId\":\"$task_id\""; then
        echo -e "${GREEN}✓ Transaction has correct task ID${NC}"
    else
        echo -e "${RED}✗ Transaction has incorrect task ID${NC}"
        exit 1
    fi
    
    # Verify user ID
    if echo "$transactions_response" | grep -q "\"userId\":\"$user_id\""; then
        echo -e "${GREEN}✓ Transaction has correct user ID${NC}"
    else
        echo -e "${RED}✗ Transaction has incorrect user ID${NC}"
        exit 1
    fi
else
    echo -e "\n${RED}✗ No transaction record found with event type TASK_COMPLETED${NC}"
    exit 1
fi

echo -e "\n${GREEN}Test completed successfully - Points transaction record created and verified${NC}"
