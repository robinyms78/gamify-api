#!/bin/bash

# Test script for verifying rewards and redemption functionality
# This test covers creating rewards, redeeming rewards, and verifying redemption status changes

# Base URL - change this to match your environment
BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Rewards and Redemption Test${NC}"
echo "========================================"

# Step 1: Create a test user
echo -e "\n${YELLOW}Step 1: Creating test user${NC}"

sleep 2

# Create unique username and email using timestamp
timestamp=$(date +%s)
username="rewarduser-$timestamp"
email="rewarduser-$timestamp@example.com"

# Create a properly formatted JSON payload for user creation
fixed_user_data="{\"username\":\"$username\",\"email\":\"$email\",\"password\":\"password123\",\"role\":\"EMPLOYEE\",\"department\":\"Engineering\"}"

echo -e "\n${YELLOW}Sending user creation request:${NC}"
echo "$fixed_user_data"

# Send the request to create a user
echo "curl -v -X POST \"$BASE_URL/auth/register\" -H \"Content-Type: application/json\" -d '$fixed_user_data'"
user_response=$(curl -v -X POST "$BASE_URL/auth/register" \
    -H "Content-Type: application/json" \
    -d "$fixed_user_data" 2>&1)

# Check if successful and extract user ID
if echo "$user_response" | grep -q '"message":"User registered successfully"'; then
    echo -e "\n${GREEN}✓ User creation successful${NC}"
    # Extract the user ID
    user_id=$(echo "$user_response" | grep -o '"userId":"[^"]*"' | cut -d':' -f2 | tr -d '"')
    echo -e "${GREEN}User ID: $user_id${NC}"
else
    echo -e "\n${RED}✗ User creation failed${NC}"
    echo "Response: $user_response"
    exit 1
fi

echo "User response: $user_response"

# Step 2: Award initial points to the user (not enough for redemption)
echo -e "\n${YELLOW}Step 2: Awarding initial points to the user${NC}"

# Create a task ID with timestamp to ensure uniqueness
task_id="task-reward-$timestamp-1"

# Create a properly formatted JSON payload for task completion
task_data="{\"userId\":\"$user_id\",\"taskId\":\"$task_id\",\"event_type\":\"TASK_COMPLETED\",\"data\":{\"priority\":\"LOW\",\"description\":\"Initial task for reward test\"}}"

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
    initial_points_awarded=$(echo "$task_response" | grep -o '"pointsAwarded":[0-9]*' | cut -d':' -f2)
    echo -e "${GREEN}Initial points awarded: $initial_points_awarded${NC}"
else
    echo -e "\n${RED}✗ Task completion event sending failed${NC}"
    exit 1
fi

# Wait a short time to allow processing
sleep 1

# Get user's current points
user_points_response=$(curl -s -X GET "$BASE_URL/api/gamification/users/$user_id/points")
current_points=$(echo "$user_points_response" | grep -o '"points":[0-9]*' | cut -d':' -f2)
echo -e "${GREEN}Current user points: $current_points${NC}"

# Step 3: Create a test reward
echo -e "\n${YELLOW}Step 3: Creating a test reward${NC}"

sleep 2

# Set reward cost higher than current points to test insufficient points scenario
reward_cost=$((current_points + 50))
reward_name="Extra Day Off - Test $timestamp"

# Create a properly formatted JSON payload for reward creation
reward_data="{\"name\":\"$reward_name\",\"description\":\"An extra day off work as a reward for good performance\",\"costInPoints\":$reward_cost,\"available\":true}"

echo -e "\n${YELLOW}Sending reward creation request:${NC}"
echo "$reward_data"

# Send the request to create a reward
reward_response=$(curl -s -X POST "$BASE_URL/rewards" \
    -H "Content-Type: application/json" \
    -d "$reward_data")

# Check if successful and extract reward ID
if echo "$reward_response" | grep -q '"id"'; then
    echo -e "\n${GREEN}✓ Reward creation successful${NC}"
    # Extract the reward ID
    reward_id=$(echo "$reward_response" | grep -o '"id":"[^"]*"' | cut -d':' -f2 | tr -d '"')
    echo -e "${GREEN}Reward ID: $reward_id${NC}"
    echo -e "${GREEN}Reward cost: $reward_cost points${NC}"
else
    echo -e "\n${RED}✗ Reward creation failed${NC}"
    echo "$reward_response"
    exit 1
fi

# Step 4: Verify insufficient points scenario
echo -e "\n${YELLOW}Step 4: Testing redemption with insufficient points${NC}"

# Create redemption request
redemption_request="{\"userId\":\"$user_id\",\"rewardId\":\"$reward_id\"}"

echo -e "\n${YELLOW}Sending redemption request with insufficient points:${NC}"
echo "$redemption_request"

# Send the request
insufficient_redemption_response=$(curl -s -X POST "$BASE_URL/rewards/redeem" \
    -H "Content-Type: application/json" \
    -d "$redemption_request")

# Display response
echo -e "\n${YELLOW}Response:${NC}"
echo "$insufficient_redemption_response"

# Check if rejected as expected
if echo "$insufficient_redemption_response" | grep -q '"success":false' && echo "$insufficient_redemption_response" | grep -q "Insufficient points"; then
    echo -e "\n${GREEN}✓ Redemption correctly rejected due to insufficient points${NC}"
else
    echo -e "\n${RED}✗ Unexpected response for insufficient points scenario${NC}"
    exit 1
fi

# Step 5: Award more points to the user
echo -e "\n${YELLOW}Step 5: Awarding more points to the user${NC}"

# Create another task ID
task_id="task-reward-$timestamp-2"

# Create a properly formatted JSON payload for high-priority task completion
task_data="{\"userId\":\"$user_id\",\"taskId\":\"$task_id\",\"event_type\":\"TASK_COMPLETED\",\"data\":{\"priority\":\"HIGH\",\"description\":\"High priority task for reward test\"}}"

echo -e "\n${YELLOW}Sending high-priority task completion request:${NC}"
echo "$task_data"

# Send the request
task_response=$(curl -s -X POST "$BASE_URL/tasks/events" \
    -H "Content-Type: application/json" \
    -d "$task_data")

# Check if successful and extract points awarded
if echo "$task_response" | grep -q '"success":true'; then
    echo -e "\n${GREEN}✓ High-priority task completion event sent successfully${NC}"
    # Extract points awarded
    additional_points=$(echo "$task_response" | grep -o '"pointsAwarded":[0-9]*' | cut -d':' -f2)
    echo -e "${GREEN}Additional points awarded: $additional_points${NC}"
else
    echo -e "\n${RED}✗ High-priority task completion event sending failed${NC}"
    exit 1
fi

# Wait a short time to allow processing
sleep 1

# Get user's updated points
user_points_response=$(curl -s -X GET "$BASE_URL/api/gamification/users/$user_id/points")
updated_points=$(echo "$user_points_response" | grep -o '"points":[0-9]*' | cut -d':' -f2)
echo -e "${GREEN}Updated user points: $updated_points${NC}"

# If still not enough points, award more
if [ "$updated_points" -lt "$reward_cost" ]; then
    echo -e "\n${YELLOW}Still need more points. Awarding additional points...${NC}"
    
    # Create another task ID
    task_id="task-reward-$timestamp-3"
    
    # Create a properly formatted JSON payload for another high-priority task
    task_data="{\"userId\":\"$user_id\",\"taskId\":\"$task_id\",\"event_type\":\"TASK_COMPLETED\",\"data\":{\"priority\":\"HIGH\",\"description\":\"Additional high priority task for reward test\"}}"
    
    # Send the request
    task_response=$(curl -s -X POST "$BASE_URL/tasks/events" \
        -H "Content-Type: application/json" \
        -d "$task_data")
    
    # Check if successful
    if echo "$task_response" | grep -q '"success":true'; then
        echo -e "\n${GREEN}✓ Additional task completion event sent successfully${NC}"
        # Extract points awarded
        more_points=$(echo "$task_response" | grep -o '"pointsAwarded":[0-9]*' | cut -d':' -f2)
        echo -e "${GREEN}More points awarded: $more_points${NC}"
        
        # Wait a short time to allow processing
        sleep 1
        
        # Get user's updated points again
        user_points_response=$(curl -s -X GET "$BASE_URL/api/gamification/users/$user_id/points")
        updated_points=$(echo "$user_points_response" | grep -o '"points":[0-9]*' | cut -d':' -f2)
        echo -e "${GREEN}Updated user points: $updated_points${NC}"
    else
        echo -e "\n${RED}✗ Additional task completion event sending failed${NC}"
        exit 1
    fi
fi

# Step 6: Redeem the reward with sufficient points
echo -e "\n${YELLOW}Step 6: Redeeming reward with sufficient points${NC}"

# Verify we have enough points now
if [ "$updated_points" -lt "$reward_cost" ]; then
    echo -e "\n${RED}✗ Still don't have enough points to redeem the reward${NC}"
    echo -e "${RED}  Current points: $updated_points, Required: $reward_cost${NC}"
    exit 1
fi

# Create redemption request
redemption_request="{\"userId\":\"$user_id\",\"rewardId\":\"$reward_id\"}"

echo -e "\n${YELLOW}Sending redemption request with sufficient points:${NC}"
echo "$redemption_request"

# Send the request
redemption_response=$(curl -s -X POST "$BASE_URL/rewards/redeem" \
    -H "Content-Type: application/json" \
    -d "$redemption_request")

# Display response
echo -e "\n${YELLOW}Response:${NC}"
echo "$redemption_response"

# Check if successful and extract redemption ID
if echo "$redemption_response" | grep -q '"success":true'; then
    echo -e "\n${GREEN}✓ Reward redemption successful${NC}"
    # Extract the redemption ID
    redemption_id=$(echo "$redemption_response" | grep -o '"redemptionId":"[^"]*"' | cut -d':' -f2 | tr -d '"')
    echo -e "${GREEN}Redemption ID: $redemption_id${NC}"
    
    # Extract updated points balance
    new_balance=$(echo "$redemption_response" | grep -o '"updatedPointsBalance":[0-9]*' | cut -d':' -f2)
    echo -e "${GREEN}New points balance: $new_balance${NC}"
    
    # Verify points were deducted correctly
    expected_balance=$((updated_points - reward_cost))
    if [ "$new_balance" -eq "$expected_balance" ]; then
        echo -e "${GREEN}✓ Points deducted correctly${NC}"
    else
        echo -e "${RED}✗ Points not deducted correctly${NC}"
        echo -e "${RED}  Expected: $expected_balance, Actual: $new_balance${NC}"
        exit 1
    fi
else
    echo -e "\n${RED}✗ Reward redemption failed${NC}"
    exit 1
fi

# Step 7: Verify redemption record
echo -e "\n${YELLOW}Step 7: Verifying redemption record${NC}"

# Get the redemption record
redemption_record_response=$(curl -s -X GET "$BASE_URL/rewards/redemption/$redemption_id")

echo -e "\n${YELLOW}Redemption record:${NC}"
echo "$redemption_record_response"

# Check if record exists with correct properties
if echo "$redemption_record_response" | grep -q "\"id\":\"$redemption_id\""; then
    echo -e "\n${GREEN}✓ Redemption record found${NC}"
    
    # Verify user ID
    if echo "$redemption_record_response" | grep -q "\"user\".*\"id\":\"$user_id\""; then
        echo -e "${GREEN}✓ Redemption has correct user ID${NC}"
    else
        echo -e "${RED}✗ Redemption has incorrect user ID${NC}"
        exit 1
    fi
    
    # Verify reward ID
    if echo "$redemption_record_response" | grep -q "\"reward\".*\"id\":\"$reward_id\""; then
        echo -e "${GREEN}✓ Redemption has correct reward ID${NC}"
    else
        echo -e "${RED}✗ Redemption has incorrect reward ID${NC}"
        exit 1
    fi
    
    # Verify initial status is PROCESSING
    if echo "$redemption_record_response" | grep -q "\"status\":\"PROCESSING\""; then
        echo -e "${GREEN}✓ Redemption has correct initial status (PROCESSING)${NC}"
    else
        echo -e "${RED}✗ Redemption has incorrect initial status${NC}"
        exit 1
    fi
else
    echo -e "\n${RED}✗ Redemption record not found${NC}"
    exit 1
fi

# Step 8: Complete the redemption
echo -e "\n${YELLOW}Step 8: Completing the redemption${NC}"

# Send request to complete the redemption
complete_response=$(curl -s -X POST "$BASE_URL/rewards/redemption/$redemption_id/complete")

echo -e "\n${YELLOW}Complete redemption response:${NC}"
echo "$complete_response"

# Check if successful
if echo "$complete_response" | grep -q '"success":true'; then
    echo -e "\n${GREEN}✓ Redemption completion successful${NC}"
else
    echo -e "\n${RED}✗ Redemption completion failed${NC}"
    exit 1
fi

# Verify status changed to COMPLETED
updated_record_response=$(curl -s -X GET "$BASE_URL/rewards/redemption/$redemption_id")
if echo "$updated_record_response" | grep -q "\"status\":\"COMPLETED\""; then
    echo -e "${GREEN}✓ Redemption status correctly updated to COMPLETED${NC}"
else
    echo -e "${RED}✗ Redemption status not updated correctly${NC}"
    exit 1
fi

# Step 9: Verify redemption cannot be canceled after completion
echo -e "\n${YELLOW}Step 9: Verifying redemption cannot be canceled after completion${NC}"

# Send request to cancel the completed redemption
cancel_response=$(curl -s -X POST "$BASE_URL/rewards/redemption/$redemption_id/cancel")

echo -e "\n${YELLOW}Cancel redemption response:${NC}"
echo "$cancel_response"

# Check if rejected as expected
if echo "$cancel_response" | grep -q '"success":false'; then
    echo -e "\n${GREEN}✓ Cancellation of completed redemption correctly rejected${NC}"
else
    echo -e "\n${RED}✗ Unexpected response for cancellation of completed redemption${NC}"
    exit 1
fi

# Step 10: Create and redeem another reward for cancellation test
echo -e "\n${YELLOW}Step 10: Creating another reward for cancellation test${NC}"

# Award more points first
task_id="task-reward-$timestamp-4"
task_data="{\"userId\":\"$user_id\",\"taskId\":\"$task_id\",\"event_type\":\"TASK_COMPLETED\",\"data\":{\"priority\":\"HIGH\",\"description\":\"Task for cancellation test\"}}"

# Send the request
curl -s -X POST "$BASE_URL/tasks/events" \
    -H "Content-Type: application/json" \
    -d "$task_data" > /dev/null

# Wait a short time
sleep 1

# Get user's current points
user_points_response=$(curl -s -X GET "$BASE_URL/api/gamification/users/$user_id/points")
current_points=$(echo "$user_points_response" | grep -o '"points":[0-9]*' | cut -d':' -f2)
echo -e "${GREEN}Current user points: $current_points${NC}"

# Create another reward with lower cost
reward_cost=$((current_points / 2))
reward_name="Coffee Voucher - Test $timestamp"

reward_data="{\"name\":\"$reward_name\",\"description\":\"A voucher for a free coffee\",\"costInPoints\":$reward_cost,\"available\":true}"

# Send the request to create a reward
reward_response=$(curl -s -X POST "$BASE_URL/rewards" \
    -H "Content-Type: application/json" \
    -d "$reward_data")

# Extract the reward ID
reward_id2=$(echo "$reward_response" | grep -o '"id":"[^"]*"' | cut -d':' -f2 | tr -d '"')
echo -e "${GREEN}Second Reward ID: $reward_id2${NC}"

# Redeem the second reward
redemption_request="{\"userId\":\"$user_id\",\"rewardId\":\"$reward_id2\"}"

# Send the request
redemption_response=$(curl -s -X POST "$BASE_URL/rewards/redeem" \
    -H "Content-Type: application/json" \
    -d "$redemption_request")

# Extract the redemption ID
redemption_id2=$(echo "$redemption_response" | grep -o '"redemptionId":"[^"]*"' | cut -d':' -f2 | tr -d '"')
echo -e "${GREEN}Second Redemption ID: $redemption_id2${NC}"

# Step 11: Cancel the second redemption
echo -e "\n${YELLOW}Step 11: Canceling the second redemption${NC}"

# Send request to cancel the redemption
cancel_response=$(curl -s -X POST "$BASE_URL/rewards/redemption/$redemption_id2/cancel")

echo -e "\n${YELLOW}Cancel redemption response:${NC}"
echo "$cancel_response"

# Check if successful
if echo "$cancel_response" | grep -q '"success":true'; then
    echo -e "\n${GREEN}✓ Redemption cancellation successful${NC}"
else
    echo -e "\n${RED}✗ Redemption cancellation failed${NC}"
    exit 1
fi

# Verify status changed to CANCELLED
updated_record_response=$(curl -s -X GET "$BASE_URL/rewards/redemption/$redemption_id2")
if echo "$updated_record_response" | grep -q "\"status\":\"CANCELLED\""; then
    echo -e "${GREEN}✓ Redemption status correctly updated to CANCELLED${NC}"
else
    echo -e "${RED}✗ Redemption status not updated correctly${NC}"
    exit 1
fi

echo -e "\n${GREEN}All tests completed successfully!${NC}"
echo -e "${GREEN}Rewards and redemption functionality is working as expected.${NC}"
