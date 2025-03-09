#!/bin/bash

# Simple test script for the Task Completion & Points Awarding feature

# Base URL - change this to match your environment
BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Task Completion & Points Awarding Feature Test${NC}"
echo "========================================"

# Test HIGH priority task completion
echo -e "\n${YELLOW}Testing HIGH priority task completion:${NC}"

# Create a properly formatted JSON payload
json_data='{"userId":"user123","taskId":"task-high-123","event_type":"TASK_COMPLETED","data":{"priority":"HIGH","description":"Test task completion"}}'

echo -e "\n${YELLOW}Sending request:${NC}"
echo "$json_data"

# Send the request
response=$(curl -s -X POST "$BASE_URL/tasks/events" \
    -H "Content-Type: application/json" \
    -d "$json_data")

# Display response
echo -e "\n${YELLOW}Response:${NC}"
echo "$response"

# Check if successful
if echo "$response" | grep -q '"success":true'; then
    echo -e "\n${GREEN}✓ Task completion successful${NC}"
else
    echo -e "\n${RED}✗ Task completion failed${NC}"
fi

# Check user points
echo -e "\n${YELLOW}Checking points for user user123:${NC}"
points_response=$(curl -s -X GET "$BASE_URL/api/gamification/users/user123/points")
echo "$points_response"

echo -e "\n${GREEN}Test completed${NC}"
