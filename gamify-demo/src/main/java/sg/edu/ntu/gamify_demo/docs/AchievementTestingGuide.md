# Achievement Tracking Testing Guide

This guide outlines the testing strategy for the Achievement Tracking feature, including unit tests, integration tests, and API tests.

## Testing Strategy

The testing strategy follows a pyramid approach:

1. **Unit Tests**: Test individual components in isolation.
2. **Integration Tests**: Test interactions between components.
3. **API Tests**: Test the REST endpoints.

## Unit Tests

### Strategy Components

#### AchievementCriteriaStrategy Tests

Test each strategy implementation separately:

```java
@ExtendWith(MockitoExtension.class)
public class PointsThresholdStrategyTest {
    
    @InjectMocks
    private PointsThresholdStrategy strategy;
    
    @Test
    public void testEvaluate_UserMeetsThreshold_ReturnsTrue() {
        // Arrange
        User user = new User();
        user.setEarnedPoints(100);
        
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode criteria = objectMapper.createObjectNode();
        criteria.put("type", "POINTS_THRESHOLD");
        criteria.put("threshold", 50);
        
        // Act
        boolean result = strategy.evaluate(user, criteria);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    public void testEvaluate_UserBelowThreshold_ReturnsFalse() {
        // Arrange
        User user = new User();
        user.setEarnedPoints(30);
        
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode criteria = objectMapper.createObjectNode();
        criteria.put("type", "POINTS_THRESHOLD");
        criteria.put("threshold", 50);
        
        // Act
        boolean result = strategy.evaluate(user, criteria);
        
        // Assert
        assertFalse(result);
    }
}
```

#### AchievementCriteriaEvaluator Tests

Test the evaluator with mock strategies:

```java
@ExtendWith(MockitoExtension.class)
public class AchievementCriteriaEvaluatorTest {
    
    @Mock
    private PointsThresholdStrategy pointsThresholdStrategy;
    
    @Mock
    private TaskCompletionStrategy taskCompletionStrategy;
    
    @Mock
    private ConsecutiveDaysStrategy consecutiveDaysStrategy;
    
    @InjectMocks
    private AchievementCriteriaEvaluator evaluator;
    
    @Test
    public void testEvaluateCriteria_PointsThreshold_DelegatesToStrategy() {
        // Arrange
        User user = new User();
        
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode criteria = objectMapper.createObjectNode();
        criteria.put("type", "POINTS_THRESHOLD");
        
        when(pointsThresholdStrategy.evaluate(user, criteria)).thenReturn(true);
        
        // Act
        boolean result = evaluator.evaluateCriteria(user, criteria);
        
        // Assert
        assertTrue(result);
        verify(pointsThresholdStrategy).evaluate(user, criteria);
        verifyNoInteractions(taskCompletionStrategy, consecutiveDaysStrategy);
    }
}
```

### Factory Components

#### AchievementFactory Tests

Test the factory methods:

```java
public class AchievementFactoryTest {
    
    private AchievementFactory factory = new AchievementFactory();
    
    @Test
    public void testCreateAchievement_ReturnsAchievementWithGeneratedId() {
        // Arrange
        String name = "Test Achievement";
        String description = "Test Description";
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode criteria = objectMapper.createObjectNode();
        
        // Act
        Achievement achievement = factory.createAchievement(name, description, criteria);
        
        // Assert
        assertNotNull(achievement);
        assertNotNull(achievement.getAchievementId());
        assertEquals(name, achievement.getName());
        assertEquals(description, achievement.getDescription());
        assertEquals(criteria, achievement.getCriteria());
    }
}
```

### Event Components

#### DefaultEventPublisher Tests

Test the event publisher with mock listeners:

```java
@ExtendWith(MockitoExtension.class)
public class DefaultEventPublisherTest {
    
    @Mock
    private EventListener listener1;
    
    @Mock
    private EventListener listener2;
    
    private DefaultEventPublisher publisher;
    
    @BeforeEach
    public void setUp() {
        publisher = new DefaultEventPublisher();
        
        when(listener1.getInterestedEventTypes()).thenReturn(new String[]{"EVENT_TYPE_1"});
        when(listener2.getInterestedEventTypes()).thenReturn(new String[]{"EVENT_TYPE_2"});
        
        publisher.registerListener(listener1);
        publisher.registerListener(listener2);
    }
    
    @Test
    public void testPublishEvent_NotifiesInterestedListeners() {
        // Arrange
        User user = new User();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode eventData = objectMapper.createObjectNode();
        
        // Act
        publisher.publishEvent("EVENT_TYPE_1", user, eventData);
        
        // Assert
        verify(listener1).onEvent("EVENT_TYPE_1", user, eventData);
        verifyNoInteractions(listener2);
    }
}
```

### Service Components

#### AchievementServiceImpl Tests

Test the service with mock repositories and factories:

```java
@ExtendWith(MockitoExtension.class)
public class AchievementServiceImplTest {
    
    @Mock
    private AchievementRepository achievementRepository;
    
    @Mock
    private AchievementFactory achievementFactory;
    
    @InjectMocks
    private AchievementServiceImpl achievementService;
    
    @Test
    public void testCreateAchievement_SavesAndReturnsAchievement() {
        // Arrange
        String name = "Test Achievement";
        String description = "Test Description";
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode criteria = objectMapper.createObjectNode();
        
        Achievement achievement = new Achievement();
        when(achievementFactory.createAchievement(name, description, criteria)).thenReturn(achievement);
        when(achievementRepository.save(achievement)).thenReturn(achievement);
        
        // Act
        Achievement result = achievementService.createAchievement(name, description, criteria);
        
        // Assert
        assertSame(achievement, result);
        verify(achievementFactory).createAchievement(name, description, criteria);
        verify(achievementRepository).save(achievement);
    }
}
```

#### UserAchievementServiceImpl Tests

Test the service with mock repositories, factories, and evaluators:

```java
@ExtendWith(MockitoExtension.class)
public class UserAchievementServiceImplTest {
    
    @Mock
    private UserAchievementRepository userAchievementRepository;
    
    @Mock
    private AchievementService achievementService;
    
    @Mock
    private UserService userService;
    
    @Mock
    private UserAchievementFactory userAchievementFactory;
    
    @Mock
    private AchievementCriteriaEvaluator criteriaEvaluator;
    
    @Mock
    private AchievementMapper achievementMapper;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private UserAchievementServiceImpl userAchievementService;
    
    @Test
    public void testAwardAchievement_UserDoesNotHaveAchievement_SavesAndReturnsUserAchievement() {
        // Arrange
        User user = new User();
        Achievement achievement = new Achievement();
        ObjectNode metadata = JsonNodeFactory.instance.objectNode();
        
        UserAchievement userAchievement = new UserAchievement();
        when(userAchievementRepository.existsByUserAndAchievement(user, achievement)).thenReturn(false);
        when(userAchievementFactory.createUserAchievement(user, achievement, metadata)).thenReturn(userAchievement);
        when(userAchievementRepository.save(userAchievement)).thenReturn(userAchievement);
        
        // Act
        UserAchievement result = userAchievementService.awardAchievement(user, achievement, metadata);
        
        // Assert
        assertSame(userAchievement, result);
        verify(userAchievementFactory).createUserAchievement(user, achievement, metadata);
        verify(userAchievementRepository).save(userAchievement);
    }
    
    @Test
    public void testAwardAchievement_UserAlreadyHasAchievement_ReturnsNull() {
        // Arrange
        User user = new User();
        Achievement achievement = new Achievement();
        ObjectNode metadata = JsonNodeFactory.instance.objectNode();
        
        when(userAchievementRepository.existsByUserAndAchievement(user, achievement)).thenReturn(true);
        
        // Act
        UserAchievement result = userAchievementService.awardAchievement(user, achievement, metadata);
        
        // Assert
        assertNull(result);
        verifyNoInteractions(userAchievementFactory, userAchievementRepository);
    }
}
```

### Facade Components

#### GamificationFacade Tests

Test the facade with mock services and event publisher:

```java
@ExtendWith(MockitoExtension.class)
public class GamificationFacadeTest {
    
    @Mock
    private LadderStatusService ladderStatusService;
    
    @Mock
    private AchievementService achievementService;
    
    @Mock
    private UserAchievementService userAchievementService;
    
    @Mock
    private UserService userService;
    
    @Mock
    private EventPublisher eventPublisher;
    
    @InjectMocks
    private GamificationFacade gamificationFacade;
    
    @Test
    public void testGetUserAchievements_ReturnsUserAchievementsDTO() {
        // Arrange
        String userId = "user123";
        UserAchievementDTO expectedDTO = new UserAchievementDTO();
        when(userAchievementService.getUserAchievementsDTO(userId)).thenReturn(expectedDTO);
        
        // Act
        UserAchievementDTO result = gamificationFacade.getUserAchievements(userId);
        
        // Assert
        assertSame(expectedDTO, result);
        verify(userAchievementService).getUserAchievementsDTO(userId);
    }
    
    @Test
    public void testProcessEvent_PublishesEvent() {
        // Arrange
        String eventType = "TEST_EVENT";
        String userId = "user123";
        ObjectNode eventData = JsonNodeFactory.instance.objectNode();
        User user = new User();
        
        when(userService.getUserById(userId)).thenReturn(user);
        
        // Act
        gamificationFacade.processEvent(eventType, userId, eventData);
        
        // Assert
        verify(eventPublisher).publishEvent(eventType, user, eventData);
    }
}
```

### Controller Components

#### AchievementController Tests

Test the controller with mock services and facade:

```java
@ExtendWith(MockitoExtension.class)
public class AchievementControllerTest {
    
    @Mock
    private AchievementService achievementService;
    
    @Mock
    private UserService userService;
    
    @Mock
    private UserAchievementService userAchievementService;
    
    @Mock
    private GamificationFacade gamificationFacade;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private AchievementController achievementController;
    
    @Test
    public void testGetAllAchievements_ReturnsAchievements() {
        // Arrange
        List<Achievement> expectedAchievements = new ArrayList<>();
        when(gamificationFacade.getAllAchievements()).thenReturn(expectedAchievements);
        
        // Act
        ResponseEntity<List<Achievement>> response = achievementController.getAllAchievements();
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedAchievements, response.getBody());
        verify(gamificationFacade).getAllAchievements();
    }
}
```

## Integration Tests

Integration tests verify that components work together correctly.

### AchievementServiceIntegrationTest

Test the AchievementService with real repositories:

```java
@SpringBootTest
public class AchievementServiceIntegrationTest {
    
    @Autowired
    private AchievementService achievementService;
    
    @Autowired
    private AchievementRepository achievementRepository;
    
    @BeforeEach
    public void setUp() {
        achievementRepository.deleteAll();
    }
    
    @Test
    public void testCreateAndGetAchievement() {
        // Arrange
        String name = "Test Achievement";
        String description = "Test Description";
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode criteria = objectMapper.createObjectNode();
        criteria.put("type", "POINTS_THRESHOLD");
        criteria.put("threshold", 100);
        
        // Act
        Achievement createdAchievement = achievementService.createAchievement(name, description, criteria);
        Achievement retrievedAchievement = achievementService.getAchievementById(createdAchievement.getAchievementId());
        
        // Assert
        assertNotNull(retrievedAchievement);
        assertEquals(name, retrievedAchievement.getName());
        assertEquals(description, retrievedAchievement.getDescription());
        assertEquals(criteria.get("type").asText(), retrievedAchievement.getCriteria().get("type").asText());
        assertEquals(criteria.get("threshold").asInt(), retrievedAchievement.getCriteria().get("threshold").asInt());
    }
}
```

### UserAchievementServiceIntegrationTest

Test the UserAchievementService with real repositories and services:

```java
@SpringBootTest
public class UserAchievementServiceIntegrationTest {
    
    @Autowired
    private UserAchievementService userAchievementService;
    
    @Autowired
    private AchievementService achievementService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserAchievementRepository userAchievementRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private User testUser;
    private Achievement testAchievement;
    
    @BeforeEach
    public void setUp() {
        userAchievementRepository.deleteAll();
        
        // Create test user
        testUser = userService.createUser("testuser", "test@example.com", "password", UserRole.EMPLOYEE);
        
        // Create test achievement
        ObjectNode criteria = objectMapper.createObjectNode();
        criteria.put("type", "POINTS_THRESHOLD");
        criteria.put("threshold", 100);
        testAchievement = achievementService.createAchievement("Test Achievement", "Test Description", criteria);
    }
    
    @Test
    public void testAwardAndCheckAchievement() {
        // Arrange
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("source", "test");
        
        // Act
        UserAchievement userAchievement = userAchievementService.awardAchievement(testUser, testAchievement, metadata);
        boolean hasAchievement = userAchievementService.hasAchievement(testUser, testAchievement);
        
        // Assert
        assertNotNull(userAchievement);
        assertTrue(hasAchievement);
        assertEquals(testUser, userAchievement.getUser());
        assertEquals(testAchievement, userAchievement.getAchievement());
        assertEquals("test", userAchievement.getMetadata().get("source").asText());
    }
}
```

## API Tests

API tests verify that the REST endpoints work correctly.

### AchievementControllerIntegrationTest

Test the AchievementController endpoints:

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AchievementControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private AchievementRepository achievementRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserAchievementRepository userAchievementRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private User testUser;
    private Achievement testAchievement;
    
    @BeforeEach
    public void setUp() {
        userAchievementRepository.deleteAll();
        achievementRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test user
        testUser = new User();
        testUser.setId("user123");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedpassword");
        testUser.setRole(UserRole.EMPLOYEE);
        userRepository.save(testUser);
        
        // Create test achievement
        testAchievement = new Achievement();
        testAchievement.setAchievementId("achievement123");
        testAchievement.setName("Test Achievement");
        testAchievement.setDescription("Test Description");
        ObjectNode criteria = objectMapper.createObjectNode();
        criteria.put("type", "POINTS_THRESHOLD");
        criteria.put("threshold", 100);
        testAchievement.setCriteria(criteria);
        achievementRepository.save(testAchievement);
    }
    
    @Test
    public void testGetAllAchievements() {
        // Act
        ResponseEntity<Achievement[]> response = restTemplate.getForEntity("/api/achievements", Achievement[].class);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Achievement[] achievements = response.getBody();
        assertNotNull(achievements);
        assertEquals(1, achievements.length);
        assertEquals(testAchievement.getAchievementId(), achievements[0].getAchievementId());
        assertEquals(testAchievement.getName(), achievements[0].getName());
    }
    
    @Test
    public void testGetUserAchievements() {
        // Arrange
        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setUser(testUser);
        userAchievement.setAchievement(testAchievement);
        userAchievement.setEarnedAt(LocalDateTime.now());
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("source", "test");
        userAchievement.setMetadata(metadata);
        userAchievementRepository.save(userAchievement);
        
        // Act
        ResponseEntity<UserAchievementDTO> response = restTemplate.getForEntity(
                "/api/achievements/user/{userId}", 
                UserAchievementDTO.class, 
                testUser.getId());
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserAchievementDTO dto = response.getBody();
        assertNotNull(dto);
        assertEquals(testUser.getId(), dto.getUserId());
        assertEquals(testUser.getUsername(), dto.getUsername());
        assertEquals(1, dto.getAchievements().size());
        assertEquals(testAchievement.getAchievementId(), dto.getAchievements().get(0).getId());
        assertEquals(testAchievement.getName(), dto.getAchievements().get(0).getName());
        assertTrue(dto.getAchievements().get(0).isEarned());
    }
}
```

## Shell Script Tests

Create shell scripts to test the API endpoints:

```bash
#!/bin/bash
# achievement-test.sh

# Create an achievement
echo "Creating achievement..."
ACHIEVEMENT_ID=$(curl -s -X POST -H "Content-Type: application/json" -d '{
  "name": "Task Master",
  "description": "Complete 10 tasks",
  "criteria": {
    "type": "TASK_COMPLETION_COUNT",
    "count": 10,
    "eventType": "TASK_COMPLETED"
  }
}' http://localhost:8080/api/achievements | jq -r '.achievementId')

echo "Achievement created with ID: $ACHIEVEMENT_ID"

# Get all achievements
echo "Getting all achievements..."
curl -s http://localhost:8080/api/achievements | jq

# Process an event for a user
echo "Processing event for user..."
curl -s -X POST -H "Content-Type: application/json" -d '{
  "eventType": "TASK_COMPLETED",
  "eventDetails": {
    "taskId": "task123",
    "taskName": "Complete Project"
  }
}' http://localhost:8080/api/achievements/process/user123 | jq

# Get user achievements
echo "Getting user achievements..."
curl -s http://localhost:8080/api/users/user123/achievements | jq
```

## Test Coverage

Aim for high test coverage:

- Unit tests: 80%+ coverage of all classes
- Integration tests: Cover all critical paths
- API tests: Cover all endpoints

Use JaCoCo for test coverage reporting:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.7</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Run tests and generate coverage report:

```bash
mvn clean test jacoco:report
```

View the coverage report in `target/site/jacoco/index.html`.

## Continuous Integration

Set up CI to run tests automatically:

1. Configure GitHub Actions or Jenkins to run tests on every push.
2. Fail the build if test coverage drops below the threshold.
3. Generate and publish test reports.

Example GitHub Actions workflow:

```yaml
name: Java CI with Maven

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    - name: Set up JDK 11
      uses: actions/setup-java@v2
      with:
        java-version: '11'
        distribution: 'adopt'
    - name: Build with Maven
      run: mvn -B package --file pom.xml
    - name: Test Coverage
      run: mvn jacoco:report
    - name: Upload coverage report
      uses: actions/upload-artifact@v2
      with:
        name: coverage-report
        path: target/site/jacoco/
