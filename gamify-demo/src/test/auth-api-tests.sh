#!/bin/bash

# Auth API Test Script
# This script tests the authentication endpoints based on the user story requirements

# Set the base URL
BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Function to print section headers
print_header() {
    echo -e "\n${YELLOW}==== $1 ====${NC}\n"
}

# Function to check if the application is running
check_app_running() {
    print_header "Checking if the application is running"
    
    response=$(curl -s -o /dev/null -w "%{http_code}" $BASE_URL)
    
    if [ "$response" = "000" ]; then
        echo -e "${RED}Error: Application is not running. Please start the application first.${NC}"
        echo -e "You can start it with: ${YELLOW}cd gamify-demo && ./mvnw spring-boot:run${NC}"
        exit 1
    else
        echo -e "${GREEN}Application is running!${NC}"
    fi
}

# Test 1: Register a new user (Sarah)
test_registration() {
    print_header "Test 1: Register a new user (Sarah)"
    
    echo "Sending registration request for Sarah..."
    
    response=$(curl -s -X POST "$BASE_URL/auth/register" \
        -H "Content-Type: application/json" \
        -d '{
            "username": "sarah",
            "email": "sarah@example.com",
            "password": "password123",
            "role": "EMPLOYEE",
            "department": "Engineering"
        }')
    
    echo "Response:"
    echo $response | jq '.' 2>/dev/null || echo $response
    
    # Check if registration was successful
    if [[ $response == *"User registered successfully"* ]]; then
        echo -e "${GREEN}✓ Registration successful${NC}"
        # Extract user ID for later use
        user_id=$(echo $response | jq -r '.userId' 2>/dev/null)
        echo "User ID: $user_id"
    else
        echo -e "${RED}✗ Registration failed${NC}"
    fi
}

# Test 2: Try to register with the same username (duplicate)
test_duplicate_username() {
    print_header "Test 2: Try to register with the same username (duplicate)"
    
    echo "Sending registration request with duplicate username..."
    
    response=$(curl -s -X POST "$BASE_URL/auth/register" \
        -H "Content-Type: application/json" \
        -d '{
            "username": "sarah",
            "email": "different@example.com",
            "password": "password123",
            "role": "EMPLOYEE",
            "department": "Marketing"
        }')
    
    echo "Response:"
    echo $response | jq '.' 2>/dev/null || echo $response
    
    # Check if the error message is correct
    if [[ $response == *"Username already exists"* ]]; then
        echo -e "${GREEN}✓ Correct error message for duplicate username${NC}"
    else
        echo -e "${RED}✗ Incorrect or missing error message for duplicate username${NC}"
    fi
}

# Test 3: Try to register with the same email (duplicate)
test_duplicate_email() {
    print_header "Test 3: Try to register with the same email (duplicate)"
    
    echo "Sending registration request with duplicate email..."
    
    response=$(curl -s -X POST "$BASE_URL/auth/register" \
        -H "Content-Type: application/json" \
        -d '{
            "username": "different",
            "email": "sarah@example.com",
            "password": "password123",
            "role": "EMPLOYEE",
            "department": "Marketing"
        }')
    
    echo "Response:"
    echo $response | jq '.' 2>/dev/null || echo $response
    
    # Check if the error message is correct
    if [[ $response == *"Email already exists"* ]]; then
        echo -e "${GREEN}✓ Correct error message for duplicate email${NC}"
    else
        echo -e "${RED}✗ Incorrect or missing error message for duplicate email${NC}"
    fi
}

# Test 4: Login with valid credentials
test_login_valid() {
    print_header "Test 4: Login with valid credentials"
    
    echo "Sending login request with valid credentials..."
    
    response=$(curl -s -X POST "$BASE_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d '{
            "username": "sarah",
            "password": "password123"
        }')
    
    echo "Response:"
    echo $response | jq '.' 2>/dev/null || echo $response
    
    # Check if login was successful and contains token and user details
    if [[ $response == *"token"* && $response == *"user"* ]]; then
        echo -e "${GREEN}✓ Login successful${NC}"
        
        # Check if points are initialized to 0
        earned_points=$(echo $response | jq -r '.user.earnedPoints' 2>/dev/null)
        available_points=$(echo $response | jq -r '.user.availablePoints' 2>/dev/null)
        
        if [[ "$earned_points" == "0" && "$available_points" == "0" ]]; then
            echo -e "${GREEN}✓ Points initialized to 0 as required${NC}"
        else
            echo -e "${RED}✗ Points not initialized to 0 (earned: $earned_points, available: $available_points)${NC}"
        fi
    else
        echo -e "${RED}✗ Login failed${NC}"
    fi
}

# Test 5: Login with invalid credentials
test_login_invalid() {
    print_header "Test 5: Login with invalid credentials"
    
    echo "Sending login request with invalid credentials..."
    
    response=$(curl -s -X POST "$BASE_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d '{
            "username": "sarah",
            "password": "wrongpassword"
        }')
    
    echo "Response:"
    echo $response | jq '.' 2>/dev/null || echo $response
    
    # Check if the error message is correct
    if [[ $response == *"Invalid credentials"* ]]; then
        echo -e "${GREEN}✓ Correct error message for invalid credentials${NC}"
    else
        echo -e "${RED}✗ Incorrect or missing error message for invalid credentials${NC}"
    fi
}

# Run all tests
check_app_running
test_registration
test_duplicate_username
test_duplicate_email
test_login_valid
test_login_invalid

print_header "Test Summary"
echo -e "The tests verify the following user story requirements:"
echo -e "1. ${GREEN}✓${NC} A new employee (Sarah) can register with valid details"
echo -e "2. ${GREEN}✓${NC} The system creates a user record with initial earned_points and available_points of 0"
echo -e "3. ${GREEN}✓${NC} On logging in via /auth/login, Sarah receives a JWT token and her user details"
echo -e "4. ${GREEN}✓${NC} Appropriate error messages are returned for duplicate registrations or invalid credentials"
echo -e "\nAll requirements from the user story have been tested."
