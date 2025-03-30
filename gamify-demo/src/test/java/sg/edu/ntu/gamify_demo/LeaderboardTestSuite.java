package sg.edu.ntu.gamify_demo;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite for all leaderboard-related tests.
 * This suite includes unit tests, integration tests, and end-to-end tests for the leaderboard feature.
 * 
 * The suite selects all test classes in the following packages:
 * - sg.edu.ntu.gamify_demo.services (Unit tests for service layer)
 * - sg.edu.ntu.gamify_demo.mappers (Unit tests for mapper layer)
 * - sg.edu.ntu.gamify_demo.controllers (Unit tests for controller layer)
 * - sg.edu.ntu.gamify_demo.schedulers (Unit tests for scheduler layer)
 * - sg.edu.ntu.gamify_demo.repositories (Integration tests for repository layer)
 * - sg.edu.ntu.gamify_demo.integration (End-to-end tests for API endpoints)
 */
@Suite
@SelectPackages({
    // Unit Tests
    "sg.edu.ntu.gamify_demo.services",
    "sg.edu.ntu.gamify_demo.mappers",
    "sg.edu.ntu.gamify_demo.controllers",
    "sg.edu.ntu.gamify_demo.schedulers",
    
    // Integration Tests
    "sg.edu.ntu.gamify_demo.repositories",
    
    // End-to-End Tests
    "sg.edu.ntu.gamify_demo.integration"
})
public class LeaderboardTestSuite {
    // This class serves as a test suite container and doesn't need any implementation
}
