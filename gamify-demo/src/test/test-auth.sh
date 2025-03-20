#!/bin/bash

# Test script for auth endpoints

# Base URL
BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Testing Auth Endpoints${NC}"
echo "========================================"

# Create unique username and email using timestamp
timestamp=$(date +%s)
username="test-user-$timestamp"
email="test-$timestamp@example.com"
password="Pass123!"

# Test register endpoint
echo -e "\n${YELLOW}Testing /auth/register endpoint...${NC}"
register_data="{\"username\":\"$username\",\"email\":\"$email\",\"password\":\"$password\",\"role\":\"EMPLOYEE\",\"department\":\"Engineering\"}"
echo "Request data: $register_data"

register_response=$(curl -s -X POST -H "Content-Type: application/json" -d "$register_data" "$BASE_URL/auth/register")
echo "Response: $register_response"

if [[ $register_response == *"userId"* ]]; then
  echo -e "${GREEN}✓ Registration successful${NC}"
  user_id=$(echo $register_response | grep -o '"userId":"[^"]*"' | cut -d':' -f2 | tr -d '"')
  echo -e "User ID: $user_id"
else
  echo -e "${RED}✗ Registration failed${NC}"
  exit 1
fi

# Test login endpoint
echo -e "\n${YELLOW}Testing /auth/login endpoint...${NC}"
login_data="{\"username\":\"$username\",\"password\":\"$password\"}"
echo "Request data: $login_data"

login_response=$(curl -s -X POST -H "Content-Type: application/json" -d "$login_data" "$BASE_URL/auth/login")
echo "Response: $login_response"

if [[ $login_response == *"token"* ]]; then
  echo -e "${GREEN}✓ Login successful${NC}"
  token=$(echo $login_response | grep -o '"token":"[^"]*"' | cut -d':' -f2 | tr -d '"')
  echo -e "Token: $token"
else
  echo -e "${RED}✗ Login failed${NC}"
  exit 1
fi

# Test API endpoint with token
echo -e "\n${YELLOW}Testing API endpoint with token...${NC}"
api_response=$(curl -s -X GET -H "Authorization: Bearer $token" "$BASE_URL/api/ladder/levels")
echo "Response: $api_response"

if [[ $api_response != *"error"* ]]; then
  echo -e "${GREEN}✓ API access successful${NC}"
else
  echo -e "${RED}✗ API access failed${NC}"
  exit 1
fi

echo -e "\n${GREEN}All tests passed!${NC}"
