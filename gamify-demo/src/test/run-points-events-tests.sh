#!/bin/bash

# Script to run tests for the points events refactoring

# Set the working directory to the project root
cd /home/krunchontu/SCTP/projects/gamify-api/gamify-demo || exit

# Print header
echo "====================================================="
echo "Running tests for Points Events Refactoring"
echo "====================================================="

# Run the specific tests for the refactored code
echo "Running PointsServiceTest..."
./mvnw test -Dtest=sg.edu.ntu.gamify_demo.services.PointsServiceTest

echo "Running PointsEventSubscriberTest..."
./mvnw test -Dtest=sg.edu.ntu.gamify_demo.events.domain.subscribers.PointsEventSubscriberTest

echo "Running DomainEventPublisherTest..."
./mvnw test -Dtest=sg.edu.ntu.gamify_demo.events.domain.DomainEventPublisherTest

# Run the integration tests to ensure the refactoring doesn't break existing functionality
echo "Running TaskEventIntegrationTest..."
./mvnw test -Dtest=sg.edu.ntu.gamify_demo.integration.TaskEventIntegrationTest

echo "====================================================="
echo "Tests completed"
echo "====================================================="
