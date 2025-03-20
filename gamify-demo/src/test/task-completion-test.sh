#!/bin/bash

# Test script for the Task Completion & Points Awarding feature

# Base URL - change this to match your environment
BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Task Completion & Points Awarding Feature Test${NC}"
echo "========================================"

# Function to test task completion
test_task_completion() {
    local user_id=$1
    local task_id=$2
    local priority=$3
    
    echo -e "\n${YELLOW}Testing task completion:${NC}"
    echo "User ID: $user_id"
    echo "Task ID: $task_id"
    echo "Priority: $priority"
    
    # Create request payload
    local data='{
        "userId": "'$user_id'",
        "taskId": "'$task_id'",
        "event_type": "TASK_COMPLETED",
        "data": {
            "priority": "'$priority'",
            "description": "Test task completion",
            "skip_ladder_update": true
        }
    }'
    
    # Fix JSON format (remove newlines and extra spaces)
    data=$(echo "$data" | tr -d '\n' | tr -d '\t' | sed 's/ \+/ /g')
    
    # Send request
    echo -e "\n${YELLOW}Sending request:${NC}"
    echo "$data"
    
    response=$(curl -s -X POST "$BASE_URL/tasks/events" \
        -H "Content-Type: application/json" \
        -d "$data")
    
    # Display response
    echo -e "\n${YELLOW}Response:${NC}"
    echo "$response"
    
    # Check if successful
    if echo "$response" | grep -q '"success":true'; then
        echo -e "\n${GREEN}✓ Task completion successful${NC}"
    else
        echo -e "\n${RED}✗ Task completion failed${NC}"
    fi
}

# Function to check user points
check_user_points() {
    local user_id=$1
    
    echo -e "\n${YELLOW}Checking points for user $user_id:${NC}"
    
    response=$(curl -s -X GET "$BASE_URL/api/gamification/users/$user_id/points")
    
    # Display response
    echo "$response"
    
    # Extract points
    points=$(echo "$response" | grep -o '"points":[0-9]*' | cut -d':' -f2)
    
    echo -e "\n${GREEN}User has $points points${NC}"
}

# Test with different priorities
echo -e "\n${YELLOW}Running tests with different priorities...${NC}"

# Get user ID from command line or use default
USER_ID=${1:-"user123"}

# Test LOW priority
test_task_completion "$USER_ID" "task_low_$(date +%s)" "LOW"
sleep 1

# Test MEDIUM priority
test_task_completion "$USER_ID" "task_medium_$(date +%s)" "MEDIUM"
sleep 1

# Test HIGH priority
test_task_completion "$USER_ID" "task_high_$(date +%s)" "HIGH"
sleep 1

# Test CRITICAL priority
test_task_completion "$USER_ID" "task_critical_$(date +%s)" "CRITICAL"
sleep 1

# Test with no priority (should use DEFAULT)
test_task_completion "$USER_ID" "task_default_$(date +%s)" ""

# Check final points
check_user_points "$USER_ID"

echo -e "\n${GREEN}All tests completed${NC}"
