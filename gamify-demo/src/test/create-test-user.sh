#!/bin/bash

# Script to create a test user for the Task Completion & Points Awarding feature

# Base URL - change this to match your environment
BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Creating Test User${NC}"
echo "========================================"

# Create a test user
echo -e "\n${YELLOW}Creating user with ID 'user123'${NC}"

# Create a properly formatted JSON payload for user creation
json_data='{
  "username": "testuser",
  "email": "testuser@example.com",
  "password": "password123",
  "role": "EMPLOYEE",
  "department": "Engineering"
}'

echo -e "\n${YELLOW}Sending request:${NC}"
echo "$json_data"

# Send the request to create a user
response=$(curl -s -X POST "$BASE_URL/auth/register" \
    -H "Content-Type: application/json" \
    -d "$json_data")

# Display response
echo -e "\n${YELLOW}Response:${NC}"
echo "$response"

# Check if successful
if echo "$response" | grep -q '"message":"User registered successfully"'; then
    echo -e "\n${GREEN}✓ User creation successful${NC}"
    # Extract the user ID
    user_id=$(echo "$response" | grep -o '"userId":"[^"]*"' | cut -d':' -f2 | tr -d '"')
    echo -e "${GREEN}User ID: $user_id${NC}"
else
    echo -e "\n${RED}✗ User creation failed${NC}"
fi

echo -e "\n${GREEN}Test user creation completed${NC}"
