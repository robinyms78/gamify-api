#!/bin/bash
# achievement-test.sh
# Script to test the Achievement Tracking API endpoints

# Set the base URL
BASE_URL="http://localhost:8080/api"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print section headers
print_header() {
    echo -e "\n${YELLOW}==== $1 ====${NC}\n"
}

# Function to check if the API is running
check_api() {
    print_header "Checking if API is running"
    
    response=$(curl -s -o /dev/null -w "%{http_code}" $BASE_URL/achievements)
    
    if [ $response -eq 200 ]; then
        echo -e "${GREEN}API is running${NC}"
    else
        echo -e "${RED}API is not running. Please start the application first.${NC}"
        exit 1
    fi
}

# Function to create a test user
create_test_user() {
    print_header "Creating test user"
    
    USER_ID=$(curl -s -X POST -H "Content-Type: application/json" -d '{
        "username": "testuser",
        "email": "test@example.com",
        "password": "password",
        "role": "EMPLOYEE",
        "department": "Engineering"
    }' $BASE_URL/auth/register | jq -r '.userId')
    
    if [ "$USER_ID" != "null" ] && [ "$USER_ID" != "" ]; then
        echo -e "${GREEN}User created with ID: $USER_ID${NC}"
    else
        echo -e "${RED}Failed to create user${NC}"
        # Try to get existing user
        USER_ID="user123"
        echo -e "${YELLOW}Using default user ID: $USER_ID${NC}"
    fi
    
    echo $USER_ID
}

# Function to create an achievement
create_achievement() {
    print_header "Creating achievement"
    
    ACHIEVEMENT_ID=$(curl -s -X POST -H "Content-Type: application/json" -d '{
        "name": "Task Master",
        "description": "Complete 10 tasks",
        "criteria": {
            "type": "TASK_COMPLETION_COUNT",
            "count": 10,
            "eventType": "TASK_COMPLETED"
        }
    }' $BASE_URL/achievements | jq -r '.achievementId')
    
    if [ "$ACHIEVEMENT_ID" != "null" ] && [ "$ACHIEVEMENT_ID" != "" ]; then
        echo -e "${GREEN}Achievement created with ID: $ACHIEVEMENT_ID${NC}"
    else
        echo -e "${RED}Failed to create achievement${NC}"
        exit 1
    fi
    
    echo $ACHIEVEMENT_ID
}

# Function to get all achievements
get_all_achievements() {
    print_header "Getting all achievements"
    
    curl -s $BASE_URL/achievements | jq
}

# Function to get an achievement by ID
get_achievement_by_id() {
    local achievement_id=$1
    
    print_header "Getting achievement by ID: $achievement_id"
    
    curl -s $BASE_URL/achievements/$achievement_id | jq
}

# Function to process an event for a user
process_event() {
    local user_id=$1
    local event_type=$2
    
    print_header "Processing $event_type event for user: $user_id"
    
    curl -s -X POST -H "Content-Type: application/json" -d "{
        \"eventType\": \"$event_type\",
        \"eventDetails\": {
            \"taskId\": \"task123\",
            \"taskName\": \"Complete Project\"
        }
    }" $BASE_URL/achievements/process/$user_id | jq
}

# Function to get user achievements
get_user_achievements() {
    local user_id=$1
    
    print_header "Getting achievements for user: $user_id"
    
    curl -s $BASE_URL/users/$user_id/achievements | jq
}

# Function to get user achievement count
get_user_achievement_count() {
    local user_id=$1
    
    print_header "Getting achievement count for user: $user_id"
    
    curl -s $BASE_URL/users/$user_id/achievements/count | jq
}

# Function to check if a user has a specific achievement
check_user_achievement() {
    local user_id=$1
    local achievement_id=$2
    
    print_header "Checking if user $user_id has achievement $achievement_id"
    
    curl -s $BASE_URL/achievements/$achievement_id/check/$user_id | jq
}

# Main test flow
main() {
    check_api
    
    # Create test user
    USER_ID=$(create_test_user)
    
    # Create achievement
    ACHIEVEMENT_ID=$(create_achievement)
    
    # Get all achievements
    get_all_achievements
    
    # Get achievement by ID
    get_achievement_by_id $ACHIEVEMENT_ID
    
    # Process events for the user
    process_event $USER_ID "TASK_COMPLETED"
    process_event $USER_ID "POINTS_EARNED"
    
    # Get user achievements
    get_user_achievements $USER_ID
    
    # Get user achievement count
    get_user_achievement_count $USER_ID
    
    # Check if user has the achievement
    check_user_achievement $USER_ID $ACHIEVEMENT_ID
    
    print_header "Test completed successfully"
}

# Run the main function
main
