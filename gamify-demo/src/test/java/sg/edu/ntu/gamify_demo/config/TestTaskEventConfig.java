package sg.edu.ntu.gamify_demo.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import com.fasterxml.jackson.databind.ObjectMapper;
import sg.edu.ntu.gamify_demo.factories.TestTaskEventCommandFactory;
import sg.edu.ntu.gamify_demo.interfaces.LadderStatusService;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.repositories.PointsTransactionRepository;
import sg.edu.ntu.gamify_demo.repositories.TaskEventRepository;
import sg.edu.ntu.gamify_demo.services.TestTaskEventService;
import sg.edu.ntu.gamify_demo.strategies.task.TaskPointsCalculationStrategy;

/**
 * Test configuration for task event integration tests.
 * Provides test-specific implementations to isolate tests from the event system.
 */
@TestConfiguration
public class TestTaskEventConfig {

    /**
     * Creates a TestTaskEventCommandFactory bean.
     * 
     * @param taskEventRepository The task event repository.
     * @param pointsTransactionRepository The points transaction repository.
     * @param pointsCalculationStrategy The points calculation strategy.
     * @param ladderStatusService The ladder status service.
     * @param objectMapper The object mapper.
     * @return A TestTaskEventCommandFactory.
     */
    @Bean
    public TestTaskEventCommandFactory testTaskEventCommandFactory(
            TaskEventRepository taskEventRepository,
            PointsTransactionRepository pointsTransactionRepository,
            TaskPointsCalculationStrategy pointsCalculationStrategy,
            LadderStatusService ladderStatusService,
            ObjectMapper objectMapper) {
        return new TestTaskEventCommandFactory(
                taskEventRepository,
                pointsTransactionRepository,
                pointsCalculationStrategy,
                ladderStatusService,
                objectMapper);
    }
    
    /**
     * Creates a TestTaskEventService bean.
     * 
     * @param taskEventRepository The task event repository.
     * @param userService The user service.
     * @param testTaskEventCommandFactory The test task event command factory.
     * @param pointsCalculationStrategy The points calculation strategy.
     * @param objectMapper The object mapper.
     * @return A TestTaskEventService.
     */
    @Bean
    @Primary
    public TestTaskEventService testTaskEventService(
            TaskEventRepository taskEventRepository,
            UserService userService,
            TestTaskEventCommandFactory testTaskEventCommandFactory,
            TaskPointsCalculationStrategy pointsCalculationStrategy,
            ObjectMapper objectMapper) {
        return new TestTaskEventService(
                taskEventRepository,
                userService,
                testTaskEventCommandFactory,
                pointsCalculationStrategy,
                objectMapper);
    }
}
