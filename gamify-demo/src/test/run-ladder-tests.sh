#!/bin/bash

# Script to run all tests for the ladder status feature

# Color codes for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Running All Tests for Ladder Status Feature${NC}"
echo "=============================================="

# Function to run a test and check the result
run_test() {
    local test_name=$1
    local test_command=$2
    
    echo -e "\n${BLUE}Running $test_name...${NC}"
    
    # Run the test command
    eval $test_command
    
    # Check the result
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ $test_name passed${NC}"
        return 0
    else
        echo -e "${RED}✗ $test_name failed${NC}"
        return 1
    fi
}

# Change to the project directory
cd "$(dirname "$0")/.."

# 1. Run unit tests for the service
run_test "LadderStatusServiceImplTest" "mvn test -Dtest=LadderStatusServiceImplTest"

# 2. Run unit tests for the controller
run_test "LadderControllerTest" "mvn test -Dtest=LadderControllerTest"

# 3. Run integration tests
run_test "LadderStatusIntegrationTest" "mvn test -Dtest=LadderStatusIntegrationTest"

# 4. Run all ladder tests together
run_test "All Ladder Tests" "mvn test -Dtest=LadderStatusServiceImplTest,LadderControllerTest,LadderStatusIntegrationTest"

echo -e "\n${YELLOW}All automated tests completed${NC}"
echo "=============================================="

# 4. Ask if the user wants to run manual tests
echo -e "\n${BLUE}Do you want to run manual API tests? (y/n)${NC}"
read -r run_manual

if [[ $run_manual =~ ^[Yy]$ ]]; then
    # Check if the application is running
    echo -e "\n${BLUE}Checking if the application is running...${NC}"
    
    if curl -s -f http://localhost:8080/api/health > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Application is running${NC}"
    else
        echo -e "${YELLOW}! Application does not seem to be running${NC}"
        echo -e "${BLUE}Do you want to start the application? (y/n)${NC}"
        read -r start_app
        
        if [[ $start_app =~ ^[Yy]$ ]]; then
            echo -e "\n${BLUE}Starting the application...${NC}"
            # Start the application in a new terminal
            gnome-terminal -- bash -c "cd $(pwd) && mvn spring-boot:run; read -p 'Press Enter to close...'" || \
            xterm -e "cd $(pwd) && mvn spring-boot:run; read -p 'Press Enter to close...'" || \
            echo -e "${RED}✗ Could not start a new terminal. Please start the application manually.${NC}"
            
            echo -e "${YELLOW}Waiting for the application to start...${NC}"
            sleep 20
        else
            echo -e "${YELLOW}Please start the application manually before running manual tests.${NC}"
            exit 0
        fi
    fi
    
    # Ask for a user ID to test with
    echo -e "\n${BLUE}Enter a user ID to test with (leave empty to create a new test user):${NC}"
    read -r user_id
    
    if [ -z "$user_id" ]; then
        echo -e "\n${BLUE}Creating a test user...${NC}"
        # Run the create-test-user.sh script if it exists
        if [ -f "$(pwd)/src/test/create-test-user.sh" ]; then
            chmod +x "$(pwd)/src/test/create-test-user.sh"
            user_response=$(bash "$(pwd)/src/test/create-test-user.sh")
            echo "$user_response"
            
            # Extract user ID from the response
            user_id=$(echo "$user_response" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
            
            if [ -n "$user_id" ]; then
                echo -e "${GREEN}✓ Created test user with ID: $user_id${NC}"
            else
                echo -e "${RED}✗ Failed to extract user ID from response${NC}"
                echo -e "${YELLOW}Please enter a user ID manually:${NC}"
                read -r user_id
            fi
        else
            echo -e "${RED}✗ create-test-user.sh script not found${NC}"
            echo -e "${YELLOW}Please enter a user ID manually:${NC}"
            read -r user_id
        fi
    fi
    
    if [ -n "$user_id" ]; then
        # Update the ladder-status-test.sh script with the user ID
        if [ -f "$(pwd)/src/test/ladder-status-test.sh" ]; then
            echo -e "\n${BLUE}Updating ladder-status-test.sh with user ID: $user_id${NC}"
            sed -i "s/USER_ID=\"[^\"]*\"/USER_ID=\"$user_id\"/" "$(pwd)/src/test/ladder-status-test.sh"
            
            # Run the ladder-status-test.sh script
            echo -e "\n${BLUE}Running ladder status tests...${NC}"
            chmod +x "$(pwd)/src/test/ladder-status-test.sh"
            bash "$(pwd)/src/test/ladder-status-test.sh"
            
            # Run the ladder-status-update-test.sh script
            echo -e "\n${BLUE}Running ladder status update tests...${NC}"
            chmod +x "$(pwd)/src/test/ladder-status-update-test.sh"
            bash "$(pwd)/src/test/ladder-status-update-test.sh"
        else
            echo -e "${RED}✗ ladder-status-test.sh script not found${NC}"
            
            # Run manual curl commands instead
            echo -e "\n${BLUE}Running manual curl commands...${NC}"
            
            echo -e "\n${YELLOW}Test 1: Get ladder status for user $user_id${NC}"
            curl -s -X GET "http://localhost:8080/api/ladder/status?userId=$user_id" | python -m json.tool
            
            echo -e "\n${YELLOW}Test 2: Get ladder status for non-existent user${NC}"
            curl -s -X GET "http://localhost:8080/api/ladder/status?userId=non-existent-user" | python -m json.tool
        fi
        
        # Test level-up functionality
        echo -e "\n${BLUE}Do you want to test the level-up functionality? (y/n)${NC}"
        read -r test_level_up
        
        if [[ $test_level_up =~ ^[Yy]$ ]]; then
            echo -e "\n${YELLOW}Current ladder status:${NC}"
            curl -s -X GET "http://localhost:8080/api/ladder/status?userId=$user_id" | python -m json.tool
            
            echo -e "\n${BLUE}Enter the number of points to set for the user (e.g., 250):${NC}"
            read -r points
            
            if [ -n "$points" ]; then
                echo -e "\n${YELLOW}Updating user points...${NC}"
                # This would normally be done through a database update
                echo -e "${RED}Note: This is a simulation. In a real environment, you would update the database.${NC}"
                echo -e "${YELLOW}SQL command: UPDATE users SET earned_points = $points WHERE id = '$user_id';${NC}"
                
            # For demonstration, we'll use the API to update the user's points if available
            echo -e "\n${YELLOW}Attempting to update points through API...${NC}"
            # Get a CSRF token first
            csrf_token=$(curl -s -c cookie.txt -X GET "http://localhost:8080/api/csrf-token" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
            
            if [ -n "$csrf_token" ]; then
                echo -e "${GREEN}✓ Got CSRF token: $csrf_token${NC}"
                curl -s -b cookie.txt -X PUT "http://localhost:8080/api/users/$user_id/points" \
                     -H "Content-Type: application/json" \
                     -H "X-CSRF-TOKEN: $csrf_token" \
                     -d "{\"points\": $points}" | python -m json.tool
                rm cookie.txt
            else
                curl -s -X PUT "http://localhost:8080/api/users/$user_id/points" \
                     -H "Content-Type: application/json" \
                     -d "{\"points\": $points}" | python -m json.tool || \
                echo -e "${RED}✗ Failed to update points through API. Please update the database manually.${NC}"
            fi
                
                echo -e "\n${YELLOW}Updated ladder status:${NC}"
                curl -s -X GET "http://localhost:8080/api/ladder/status?userId=$user_id" | python -m json.tool
            fi
        fi
    else
        echo -e "${RED}✗ No user ID provided. Cannot run tests.${NC}"
    fi
else
    echo -e "${YELLOW}Skipping manual tests.${NC}"
fi

echo -e "\n${GREEN}Testing completed!${NC}"
echo -e "${BLUE}For more details on testing, see the testing-guide.md file.${NC}"
