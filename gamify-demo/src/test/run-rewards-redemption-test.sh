#!/bin/bash

# Runner script for rewards and redemption tests

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Running Rewards and Redemption Tests${NC}"
echo "========================================"

# Get the directory of this script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Make the test script executable if it isn't already
chmod +x "$SCRIPT_DIR/rewards-redemption-test.sh"

# Run the test script
"$SCRIPT_DIR/rewards-redemption-test.sh"

# Check the exit status
if [ $? -eq 0 ]; then
    echo -e "\n${GREEN}Rewards and Redemption Tests completed successfully!${NC}"
    exit 0
else
    echo -e "\n${RED}Rewards and Redemption Tests failed!${NC}"
    exit 1
fi
