package sg.edu.ntu.gamify_demo;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

import sg.edu.ntu.gamify_demo.controllers.AuthControllerTest;
import sg.edu.ntu.gamify_demo.controllers.UserControllerTest;
import sg.edu.ntu.gamify_demo.integration.AuthIntegrationTest;
import sg.edu.ntu.gamify_demo.integration.UserIntegrationTest;
import sg.edu.ntu.gamify_demo.repositories.UserRepositoryTest;
import sg.edu.ntu.gamify_demo.services.UserServiceTest;
import sg.edu.ntu.gamify_demo.services.UserValidatorTest;

/**
 * Test suite for running all User-related tests.
 * This suite includes tests for the repository, service, controller, validator, and integration tests.
 */
@Suite
@SuiteDisplayName("User Operations Test Suite")
@SelectClasses({
    UserRepositoryTest.class,
    UserServiceTest.class,
    UserValidatorTest.class,
    UserControllerTest.class,
    UserIntegrationTest.class,
    AuthControllerTest.class,
    AuthIntegrationTest.class
})
@SelectPackages({
    "sg.edu.ntu.gamify_demo.repositories",
    "sg.edu.ntu.gamify_demo.services",
    "sg.edu.ntu.gamify_demo.controllers",
    "sg.edu.ntu.gamify_demo.integration"
})
public class UserTestSuite {
    // This class serves as a test suite container and doesn't need any implementation
}
