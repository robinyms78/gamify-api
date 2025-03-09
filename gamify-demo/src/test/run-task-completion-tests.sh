#!/bin/bash

# Master script to run all task completion feature tests
# This script runs all the individual test scripts in sequence

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}Task Completion & Points Awarding Feature Tests${NC}"
echo "========================================"

# Make all test scripts executable
chmod +x src/test/task-event-record-creation-test.sh
chmod +x src/test/points-calculation-test.sh
chmod +x src/test/points-transaction-test.sh
chmod +x src/test/ladder-status-update-test.sh

# Function to run a test script and check its result
run_test() {
    local script=$1
    local description=$2
    
    echo -e "\n${BLUE}Running Test: $description${NC}"
    echo "----------------------------------------"
    
    # Run the test script
    ./src/test/$script
    
    # Check the result
    if [ $? -eq 0 ]; then
        echo -e "\n${GREEN}✓ $description test passed${NC}"
        return 0
    else
        echo -e "\n${RED}✗ $description test failed${NC}"
        return 1
    fi
}

# Track overall success
overall_success=true

# Test 1: Task Event Record Creation
run_test "task-event-record-creation-test.sh" "Task Event Record Creation"
if [ $? -ne 0 ]; then
    overall_success=false
fi

echo -e "\n${YELLOW}Waiting 5 seconds before next test...${NC}"
sleep 5

# Test 2: Points Calculation
run_test "points-calculation-test.sh" "Points Calculation for Different Priorities"
if [ $? -ne 0 ]; then
    overall_success=false
fi

echo -e "\n${YELLOW}Waiting 5 seconds before next test...${NC}"
sleep 5

# Test 3: Points Transaction Record
run_test "points-transaction-test.sh" "Points Transaction Record Creation"
if [ $? -ne 0 ]; then
    overall_success=false
fi

echo -e "\n${YELLOW}Waiting 5 seconds before next test...${NC}"
sleep 5

# Test 4: Ladder Status Update
run_test "ladder-status-update-test.sh" "Ladder Status Update"
if [ $? -ne 0 ]; then
    overall_success=false
fi

# Print overall result
echo -e "\n${BLUE}========================================"
if [ "$overall_success" = true ]; then
    echo -e "${GREEN}All Task Completion & Points Awarding Feature tests passed!${NC}"
else
    echo -e "${RED}Some Task Completion & Points Awarding Feature tests failed!${NC}"
    echo -e "${YELLOW}Please check the individual test results above for details.${NC}"
fi
echo -e "${BLUE}========================================${NC}"
