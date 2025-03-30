#!/bin/bash

# Script to run all leaderboard tests
# This script runs the LeaderboardTestSuite which includes all unit tests, integration tests, and end-to-end tests for the leaderboard feature

# Set the working directory to the project root
cd "$(dirname "$0")/../.." || exit

# Print header
echo "====================================================="
echo "Running Leaderboard Tests"
echo "====================================================="
echo "This will run all unit tests, integration tests, and end-to-end tests for the leaderboard feature."
echo

# Run the tests
echo "Starting test execution..."
echo

# Use Maven to run the LeaderboardTestSuite
mvn test -Dtest=sg.edu.ntu.gamify_demo.LeaderboardTestSuite

# Check the exit code
if [ $? -eq 0 ]; then
    echo
    echo "====================================================="
    echo "All leaderboard tests passed successfully!"
    echo "====================================================="
else
    echo
    echo "====================================================="
    echo "Some leaderboard tests failed. Please check the output above for details."
    echo "====================================================="
fi
