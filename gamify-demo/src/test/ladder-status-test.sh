#!/bin/bash

# Test script for the ladder status endpoint
# This script tests the /api/ladder/status endpoint with a user ID

# Set variables
BASE_URL="http://localhost:8080/api"
USER_ID="test-user-id"  # Replace with an actual user ID from your database

# Color codes for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Testing Ladder Status API${NC}"
echo "=============================="

# Get a CSRF token and session cookie if needed
echo -e "\n${BLUE}Attempting to get CSRF token...${NC}"
CSRF_TOKEN=$(curl -s -c cookie.txt -X GET "${BASE_URL}/csrf-token" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -n "$CSRF_TOKEN" ]; then
    echo -e "${GREEN}✓ Got CSRF token: $CSRF_TOKEN${NC}"
    CSRF_HEADER="-H \"X-CSRF-TOKEN: $CSRF_TOKEN\""
    COOKIE="-b cookie.txt"
else
    echo -e "${YELLOW}! No CSRF token found, proceeding without it${NC}"
    CSRF_HEADER=""
    COOKIE=""
fi

# Test 1: Get ladder status for a user
echo -e "\n${YELLOW}Test 1: Get ladder status for user ${USER_ID}${NC}"
RESPONSE=$(curl -s $COOKIE -X GET "${BASE_URL}/ladder/status?userId=${USER_ID}")

# Check if the response contains the expected fields
if echo "$RESPONSE" | grep -q "currentLevel" && 
   echo "$RESPONSE" | grep -q "levelLabel" && 
   echo "$RESPONSE" | grep -q "earnedPoints" && 
   echo "$RESPONSE" | grep -q "pointsToNextLevel"; then
    echo -e "${GREEN}✓ Success: Response contains all required fields${NC}"
    echo "Response:"
    echo "$RESPONSE" | python -m json.tool
else
    echo -e "${RED}✗ Error: Response is missing required fields${NC}"
    echo "Response:"
    echo "$RESPONSE"
fi

# Test 2: Get ladder status for a non-existent user
echo -e "\n${YELLOW}Test 2: Get ladder status for non-existent user${NC}"
RESPONSE=$(curl -s $COOKIE -X GET "${BASE_URL}/ladder/status?userId=non-existent-user")

# Clean up
if [ -f "cookie.txt" ]; then
    rm cookie.txt
fi

# Check if the response contains an error message
if echo "$RESPONSE" | grep -q "error" && echo "$RESPONSE" | grep -q "User not found"; then
    echo -e "${GREEN}✓ Success: Proper error response for non-existent user${NC}"
    echo "Response:"
    echo "$RESPONSE" | python -m json.tool
else
    echo -e "${RED}✗ Error: Unexpected response for non-existent user${NC}"
    echo "Response:"
    echo "$RESPONSE"
fi

echo -e "\n${YELLOW}Tests completed${NC}"
