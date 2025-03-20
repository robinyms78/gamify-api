#!/bin/bash

# Enhanced runner script for rewards and redemption tests
# This script provides better error handling and reporting

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to log messages with timestamp
log_message() {
    local level=$1
    local message=$2
    local color=$NC
    
    case $level in
        "INFO") color=$BLUE ;;
        "SUCCESS") color=$GREEN ;;
        "ERROR") color=$RED ;;
        "WARNING") color=$YELLOW ;;
    esac
    
    echo -e "[$(date +"%Y-%m-%d %H:%M:%S")] ${color}${level}${NC}: ${message}"
}

# Function to check if the application is running
check_app_running() {
    log_message "INFO" "Checking if application is running on http://localhost:8080"
    
    if curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080" --max-time 5 | grep -q "200\|302\|401\|403"; then
        log_message "SUCCESS" "Application is running"
        return 0
    else
        log_message "ERROR" "Application does not appear to be running on http://localhost:8080"
        log_message "INFO" "Please start the application before running the tests"
        return 1
    fi
}

echo -e "${YELLOW}Running Enhanced Rewards and Redemption Tests${NC}"
echo "========================================"

# Check if application is running
if ! check_app_running; then
    exit 1
fi

# Get the directory of this script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Make the test script executable if it isn't already
chmod +x "$SCRIPT_DIR/rewards-redemption-test.sh"

# Run the test script with timeout
log_message "INFO" "Starting rewards redemption tests"
timeout 300 "$SCRIPT_DIR/rewards-redemption-test.sh"
TEST_EXIT_CODE=$?

# Check the exit status
if [ $TEST_EXIT_CODE -eq 0 ]; then
    log_message "SUCCESS" "Rewards and Redemption Tests completed successfully!"
    exit 0
elif [ $TEST_EXIT_CODE -eq 124 ]; then
    log_message "ERROR" "Rewards and Redemption Tests timed out after 5 minutes!"
    log_message "INFO" "This may indicate a problem with the API or network connectivity"
    exit 1
else
    log_message "ERROR" "Rewards and Redemption Tests failed with exit code $TEST_EXIT_CODE!"
    exit 1
fi
