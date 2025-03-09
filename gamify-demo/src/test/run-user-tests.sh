#!/bin/bash
# Script to run only user-related tests as an example of running a smaller batch

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

# Create directories for logs
LOG_DIR="target/test-logs"
mkdir -p $LOG_DIR

# Clean and compile the project
run_command "mvn clean compile" "Compiling the project"
if [ $? -ne 0 ]; then
    echo -e "${RED}Compilation failed. Please fix the errors and try again.${NC}"
    exit 1
fi

# Run only User-related tests as an example
print_header "Running User-related tests"
run_command "mvn test -Dtest.group=UserRepo -Dtest=\"sg.edu.ntu.gamify_demo.repositories.UserRepositoryTest\"" "User Repository Tests"
run_command "mvn test -Dtest.group=UserService -Dtest=\"sg.edu.ntu.gamify_demo.services.UserServiceTest\"" "User Service Tests"
run_command "mvn test -Dtest.group=UserController -Dtest=\"sg.edu.ntu.gamify_demo.controllers.UserControllerTest\"" "User Controller Tests"
run_command "mvn test -Dtest.group=UserIntegration -Dtest=\"sg.edu.ntu.gamify_demo.integration.UserIntegrationTest\"" "User Integration Tests"
run_command "mvn test -Dtest.group=AuthTests -Dtest=\"sg.edu.ntu.gamify_demo.controllers.AuthControllerTest,sg.edu.ntu.gamify_demo.integration.AuthIntegrationTest\"" "Auth Tests"

# Generate test summary report for this batch
print_header "Generating test summary report"
echo "User Tests Summary Report" > $LOG_DIR/user-tests-summary.txt
echo "=======================" >> $LOG_DIR/user-tests-summary.txt
echo "" >> $LOG_DIR/user-tests-summary.txt

# Check user-related log files for failures
for group in UserRepo UserService UserController UserIntegration AuthTests; do
    log_file="$LOG_DIR/$group.log"
    
    if [ -f "$log_file" ]; then
        if grep -q "FAILURE" "$log_file" || grep -q "ERROR" "$log_file"; then
            echo "❌ $group: FAILED" >> $LOG_DIR/user-tests-summary.txt
            echo "Failures:" >> $LOG_DIR/user-tests-summary.txt
            grep -A 3 -B 1 "FAILURE\|ERROR" "$log_file" | sed 's/^/  /' >> $LOG_DIR/user-tests-summary.txt
            echo "" >> $LOG_DIR/user-tests-summary.txt
        else
            echo "✅ $group: PASSED" >> $LOG_DIR/user-tests-summary.txt
        fi
    else
        echo "⚠️ $group: No log file found" >> $LOG_DIR/user-tests-summary.txt
    fi
done

# Print overall summary
total_tests=5
failed_tests=$(grep -c "FAILED" $LOG_DIR/user-tests-summary.txt)
passed_tests=$((total_tests - failed_tests))

echo "" >> $LOG_DIR/user-tests-summary.txt
echo "Overall Summary:" >> $LOG_DIR/user-tests-summary.txt
echo "---------------" >> $LOG_DIR/user-tests-summary.txt
echo "Total Test Groups: $total_tests" >> $LOG_DIR/user-tests-summary.txt
echo "Passed: $passed_tests" >> $LOG_DIR/user-tests-summary.txt
echo "Failed: $failed_tests" >> $LOG_DIR/user-tests-summary.txt

echo -e "${GREEN}Test execution complete. See $LOG_DIR/user-tests-summary.txt for results.${NC}"
echo -e "${YELLOW}Individual test logs are available in $LOG_DIR directory.${NC}"

# Display the summary
cat $LOG_DIR/user-tests-summary.txt
