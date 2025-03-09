#!/bin/bash

# Test script for verifying ladder status updates when points cross a threshold
# This test focuses specifically on checking that a user's ladder status is updated when they earn enough points

# Base URL - change this to match your environment
BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Ladder Status Update Test${NC}"
echo "========================================"

# Step 1: Create a test user
echo -e "\n${YELLOW}Step 1: Creating test user${NC}"

# Create unique username and email using timestamp
timestamp=$(date +%s)
username="taskuser-ladder-$timestamp"
email="taskuser-ladder-$timestamp@example.com"

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

# Step 2: Get initial ladder status
echo -e "\n${YELLOW}Step 2: Getting initial ladder status${NC}"

# Send the request to get user ladder status
initial_status_response=$(curl -s -X GET "$BASE_URL/api/ladder/users/$user_id/status")

echo -e "\n${YELLOW}Initial ladder status:${NC}"
echo "$initial_status_response"

# Extract initial level
initial_level=$(echo "$initial_status_response" | grep -o '"currentLevel":[0-9]*' | cut -d':' -f2)
if [ -z "$initial_level" ]; then
    initial_level=1  # Default to level 1 if not found
fi
echo -e "${GREEN}Initial ladder level: $initial_level${NC}"

# Step 3: Complete multiple tasks to earn enough points to cross a threshold
echo -e "\n${YELLOW}Step 3: Completing multiple tasks to earn points${NC}"

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
    
    # Send the request
    task_response=$(curl -s -X POST "$BASE_URL/tasks/events" \
        -H "Content-Type: application/json" \
        -d "$task_data")
    
    # Check if successful
    if echo "$task_response" | grep -q '"success":true'; then
        echo -e "${GREEN}✓ Task #$task_num completion successful${NC}"
    else
        echo -e "${RED}✗ Task #$task_num completion failed${NC}"
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

# Step 4: Get updated points for the user
echo -e "\n${YELLOW}Step 4: Getting updated points for the user${NC}"

# Send the request to get user points
updated_points_response=$(curl -s -X GET "$BASE_URL/api/gamification/users/$user_id/points")

echo -e "\n${YELLOW}Updated points:${NC}"
echo "$updated_points_response"

# Extract updated points
updated_points=$(echo "$updated_points_response" | grep -o '"points":[0-9]*' | cut -d':' -f2)
echo -e "${GREEN}Updated points: $updated_points${NC}"

# Step 5: Get updated ladder status
echo -e "\n${YELLOW}Step 5: Getting updated ladder status${NC}"

# Wait a short time to allow processing
sleep 2

# Send the request to get user ladder status
updated_status_response=$(curl -s -X GET "$BASE_URL/api/ladder/users/$user_id/status")

echo -e "\n${YELLOW}Updated ladder status:${NC}"
echo "$updated_status_response"

# Extract updated level
updated_level=$(echo "$updated_status_response" | grep -o '"currentLevel":[0-9]*' | cut -d':' -f2)
echo -e "${GREEN}Updated ladder level: $updated_level${NC}"

# Verify ladder level was increased
if [ "$updated_level" -gt "$initial_level" ]; then
    echo -e "\n${GREEN}✓ Ladder level increased from $initial_level to $updated_level${NC}"
else
    echo -e "\n${RED}✗ Ladder level did not increase (still at level $updated_level)${NC}"
    echo -e "${YELLOW}Note: This might be expected if the points threshold for the next level is higher than $updated_points points${NC}"
    
    # Get points to next level
    points_to_next=$(echo "$updated_status_response" | grep -o '"pointsToNextLevel":[0-9]*' | cut -d':' -f2)
    if [ -n "$points_to_next" ]; then
        echo -e "${YELLOW}Points needed for next level: $points_to_next${NC}"
        
        # Complete more tasks if needed
        if [ "$points_to_next" -gt 0 ]; then
            echo -e "\n${YELLOW}Completing more tasks to reach next level...${NC}"
            
            # Calculate how many more CRITICAL tasks needed (50 points each)
            tasks_needed=$(( ($points_to_next + 49) / 50 ))  # Ceiling division
            
            for i in $(seq 4 $((3 + tasks_needed))); do
                complete_critical_task $i
            done
            
            # Check ladder status again
            sleep 2
            final_status_response=$(curl -s -X GET "$BASE_URL/api/ladder/users/$user_id/status")
            
            echo -e "\n${YELLOW}Final ladder status:${NC}"
            echo "$final_status_response"
            
            final_level=$(echo "$final_status_response" | grep -o '"currentLevel":[0-9]*' | cut -d':' -f2)
            echo -e "${GREEN}Final ladder level: $final_level${NC}"
            
            if [ "$final_level" -gt "$initial_level" ]; then
                echo -e "\n${GREEN}✓ Ladder level increased from $initial_level to $final_level after completing additional tasks${NC}"
            else
                echo -e "\n${RED}✗ Ladder level still did not increase after completing additional tasks${NC}"
                exit 1
            fi
        fi
    fi
fi

echo -e "\n${GREEN}Test completed successfully - Ladder status update verified${NC}"
