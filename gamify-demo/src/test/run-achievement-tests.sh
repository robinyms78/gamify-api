#!/bin/bash
# Script to run achievement-related tests

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print section headers
print_header() {
    echo -e "\n${YELLOW}==== $1 ====${NC}\n"
}

# Function to run a command and check its result
run_command() {
    local command="$1"
    local description="$2"
    
    print_header "$description"
    
    echo "Running: $command"
    eval $command
    
    if [ $? -eq 0 ]; then
        echo -e "\n${GREEN}Success: $description${NC}"
        return 0
    else
        echo -e "\n${RED}Failed: $description${NC}"
        return 1
    fi
}

# Navigate to the project root directory
cd "$(dirname "$0")/../.."

# Clean and compile the project
run_command "mvn clean compile" "Compiling the project"
if [ $? -ne 0 ]; then
    echo -e "${RED}Compilation failed. Please fix the errors and try again.${NC}"
    exit 1
fi

# Run unit tests for achievement-related classes
print_header "Running unit tests for achievement-related classes"
run_command "mvn test -Dtest=\"sg.edu.ntu.gamify_demo.strategies.achievement.*Test\"" "Testing achievement strategies"
run_command "mvn test -Dtest=\"sg.edu.ntu.gamify_demo.services.AchievementServiceImplTest\"" "Testing AchievementServiceImpl"
run_command "mvn test -Dtest=\"sg.edu.ntu.gamify_demo.services.UserAchievementServiceImplTest\"" "Testing UserAchievementServiceImpl"

# Run integration tests for achievement-related classes
print_header "Running integration tests for achievement-related classes"
run_command "mvn test -Dtest=\"sg.edu.ntu.gamify_demo.integration.AchievementServiceIntegrationTest\"" "Testing AchievementService integration"

# Run all tests
print_header "Running all tests"
run_command "mvn test" "Running all tests"

# Generate test coverage report
print_header "Generating test coverage report"
run_command "mvn jacoco:report" "Generating JaCoCo report"

echo -e "\n${GREEN}All tests completed. Check the results above for any failures.${NC}"
echo -e "${YELLOW}Test coverage report is available at: target/site/jacoco/index.html${NC}"
