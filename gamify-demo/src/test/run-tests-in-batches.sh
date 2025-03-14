#!/bin/bash
# Script to run tests in smaller batches and log results to separate files

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

# Define test groups with max 5 tests per group
# User-related tests
print_header "Running User-related tests"
run_command "mvn test -Dtest.group=UserRepo -Dtest=\"sg.edu.ntu.gamify_demo.repositories.UserRepositoryTest\"" "User Repository Tests"
run_command "mvn test -Dtest.group=UserService -Dtest=\"sg.edu.ntu.gamify_demo.services.UserServiceTest\"" "User Service Tests"
run_command "mvn test -Dtest.group=UserController -Dtest=\"sg.edu.ntu.gamify_demo.controllers.UserControllerTest\"" "User Controller Tests"
run_command "mvn test -Dtest.group=UserIntegration -Dtest=\"sg.edu.ntu.gamify_demo.integration.UserIntegrationTest\"" "User Integration Tests"
run_command "mvn test -Dtest.group=AuthTests -Dtest=\"sg.edu.ntu.gamify_demo.controllers.AuthControllerTest,sg.edu.ntu.gamify_demo.integration.AuthIntegrationTest\"" "Auth Tests"

# Achievement-related tests
print_header "Running Achievement-related tests"
run_command "mvn test -Dtest.group=AchievementStrategies -Dtest=\"sg.edu.ntu.gamify_demo.strategies.achievement.*Test\"" "Achievement Strategies Tests"
run_command "mvn test -Dtest.group=AchievementService -Dtest=\"sg.edu.ntu.gamify_demo.services.AchievementServiceImplTest\"" "Achievement Service Tests"
run_command "mvn test -Dtest.group=UserAchievementService -Dtest=\"sg.edu.ntu.gamify_demo.services.UserAchievementServiceImplTest\"" "User Achievement Service Tests"
run_command "mvn test -Dtest.group=AchievementIntegration -Dtest=\"sg.edu.ntu.gamify_demo.integration.AchievementServiceIntegrationTest\"" "Achievement Integration Tests"

# Task-related tests
print_header "Running Task-related tests"
run_command "mvn test -Dtest.group=TaskEventController -Dtest=\"sg.edu.ntu.gamify_demo.controllers.TaskEventControllerTest\"" "Task Event Controller Tests"
run_command "mvn test -Dtest.group=TaskEventIntegration -Dtest=\"sg.edu.ntu.gamify_demo.integration.TaskEventIntegrationTest\"" "Task Event Integration Tests"
run_command "mvn test -Dtest.group=TaskStrategies -Dtest=\"sg.edu.ntu.gamify_demo.strategies.task.*Test\"" "Task Strategies Tests"

# Ladder/Leaderboard-related tests
print_header "Running Ladder/Leaderboard-related tests"
run_command "mvn test -Dtest.group=LadderService -Dtest=\"sg.edu.ntu.gamify_demo.services.LadderStatusServiceImplTest\"" "Ladder Status Service Tests"
run_command "mvn test -Dtest.group=LadderController -Dtest=\"sg.edu.ntu.gamify_demo.controllers.LadderControllerTest\"" "Ladder Controller Tests"
run_command "mvn test -Dtest.group=LadderIntegration -Dtest=\"sg.edu.ntu.gamify_demo.integration.LadderStatusIntegrationTest\"" "Ladder Status Integration Tests"

# Points/Transaction-related tests
print_header "Running Points/Transaction-related tests"
run_command "mvn test -Dtest.group=PointsService -Dtest=\"sg.edu.ntu.gamify_demo.services.PointsServiceTest\"" "Points Service Tests"
run_command "mvn test -Dtest.group=PointsEvents -Dtest=\"sg.edu.ntu.gamify_demo.events.domain.subscribers.PointsEventSubscriberTest\"" "Points Event Subscriber Tests"

# Reward-related tests
print_header "Running Reward-related tests"
run_command "mvn test -Dtest.group=RewardService -Dtest=\"sg.edu.ntu.gamify_demo.services.RewardServiceWithLoggingImplTest\"" "Reward Service Tests"
run_command "mvn test -Dtest.group=RewardRedemption -Dtest=\"sg.edu.ntu.gamify_demo.services.RewardRedemptionServiceTest\"" "Reward Redemption Service Tests"
run_command "mvn test -Dtest.group=RewardController -Dtest=\"sg.edu.ntu.gamify_demo.controllers.RewardControllerWithLoggingImplTest\"" "Reward Controller Tests"
run_command "$(dirname "$0")/run-rewards-redemption-test.sh" "Reward Redemption Integration Tests"

# Domain Event-related tests
print_header "Running Domain Event-related tests"
run_command "mvn test -Dtest.group=DomainEvents -Dtest=\"sg.edu.ntu.gamify_demo.events.domain.DomainEventPublisherTest\"" "Domain Event Publisher Tests"
run_command "mvn test -Dtest.group=EventSubscribers -Dtest=\"sg.edu.ntu.gamify_demo.events.domain.subscribers.*Test\"" "Event Subscribers Tests"

# Generate test summary report
print_header "Generating test summary report"
echo "Test Summary Report" > $LOG_DIR/summary.txt
echo "===================" >> $LOG_DIR/summary.txt
echo "" >> $LOG_DIR/summary.txt

# List all log files and check for failures
for log_file in $LOG_DIR/*.log; do
    group_name=$(basename "$log_file" .log)
    
    if grep -q "FAILURE" "$log_file" || grep -q "ERROR" "$log_file"; then
        echo "❌ $group_name: FAILED" >> $LOG_DIR/summary.txt
        echo "Failures:" >> $LOG_DIR/summary.txt
        grep -A 3 -B 1 "FAILURE\|ERROR" "$log_file" | sed 's/^/  /' >> $LOG_DIR/summary.txt
        echo "" >> $LOG_DIR/summary.txt
    else
        echo "✅ $group_name: PASSED" >> $LOG_DIR/summary.txt
    fi
done

# Print overall summary
total_tests=$(grep -c "Running test" $LOG_DIR/summary.txt)
failed_tests=$(grep -c "FAILED" $LOG_DIR/summary.txt)
passed_tests=$((total_tests - failed_tests))

echo "" >> $LOG_DIR/summary.txt
echo "Overall Summary:" >> $LOG_DIR/summary.txt
echo "---------------" >> $LOG_DIR/summary.txt
echo "Total Test Groups: $total_tests" >> $LOG_DIR/summary.txt
echo "Passed: $passed_tests" >> $LOG_DIR/summary.txt
echo "Failed: $failed_tests" >> $LOG_DIR/summary.txt

echo -e "${GREEN}Test execution complete. See $LOG_DIR/summary.txt for results.${NC}"
echo -e "${YELLOW}Individual test logs are available in $LOG_DIR directory.${NC}"

# Display the summary
cat $LOG_DIR/summary.txt
