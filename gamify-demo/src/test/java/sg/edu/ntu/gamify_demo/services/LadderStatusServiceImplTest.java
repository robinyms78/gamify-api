package sg.edu.ntu.gamify_demo.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sg.edu.ntu.gamify_demo.dtos.LadderStatusDTO;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserLadderStatus;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;
import sg.edu.ntu.gamify_demo.repositories.LadderLevelRepository;
import sg.edu.ntu.gamify_demo.repositories.UserLadderStatusRepository;
import sg.edu.ntu.gamify_demo.strategies.PointsCalculationStrategy;

@ExtendWith(MockitoExtension.class)
public class LadderStatusServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private LadderLevelRepository ladderLevelRepository;

    @Mock
    private UserLadderStatusRepository userLadderStatusRepository;

    @Mock
    private PointsCalculationStrategy pointsCalculationStrategy;

    @InjectMocks
    private LadderStatusServiceImpl ladderStatusService;

    private User testUser;
    private LadderLevel level1;
    private LadderLevel level2;
    private UserLadderStatus userLadderStatus;

    @BeforeEach
    public void setup() {
        // Setup test data
        testUser = User.builder()
                .id("test-user-id")
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .role(UserRole.EMPLOYEE)
                .department("IT")
                .earnedPoints(100)
                .availablePoints(50)
                .build();

        level1 = new LadderLevel(1, "Beginner", 0);
        level2 = new LadderLevel(2, "Intermediate", 200);

        userLadderStatus = new UserLadderStatus(testUser, level1, 100, 100);
    }

    @Test
    public void testGetUserLadderStatus_UserExists_ReturnsDTO() {
        // Arrange
        when(userLadderStatusRepository.findById(anyString())).thenReturn(Optional.of(userLadderStatus));

        // Act
        LadderStatusDTO result = ladderStatusService.getUserLadderStatus("test-user-id");

        // Assert
        assertEquals(1, result.getCurrentLevel());
        assertEquals("Beginner", result.getLevelLabel());
        assertEquals(100, result.getEarnedPoints());
        assertEquals(100, result.getPointsToNextLevel());
    }

    @Test
    public void testGetUserLadderStatus_UserDoesNotExist_ReturnsNull() {
        // Arrange
        when(userLadderStatusRepository.findById(anyString())).thenReturn(Optional.empty());
        when(userService.getUserById(anyString())).thenReturn(null);

        // Act
        LadderStatusDTO result = ladderStatusService.getUserLadderStatus("non-existent-user");

        // Assert
        assertNull(result);
    }

    @Test
    public void testGetUserLadderStatus_NewUser_InitializesAndReturnsDTO() {
        // Arrange
        when(userLadderStatusRepository.findById(anyString())).thenReturn(Optional.empty());
        when(userService.getUserById(anyString())).thenReturn(testUser);
        when(ladderLevelRepository.findByLevel(1)).thenReturn(level1);
        when(ladderLevelRepository.findAllByOrderByLevelAsc()).thenReturn(Arrays.asList(level1, level2));
        when(pointsCalculationStrategy.calculatePointsToNextLevel(anyInt(), any(), any())).thenReturn(100);
        when(userLadderStatusRepository.save(any(UserLadderStatus.class))).thenReturn(userLadderStatus);

        // Act
        LadderStatusDTO result = ladderStatusService.getUserLadderStatus("test-user-id");

        // Assert
        assertEquals(1, result.getCurrentLevel());
        assertEquals("Beginner", result.getLevelLabel());
        assertEquals(100, result.getEarnedPoints());
        assertEquals(100, result.getPointsToNextLevel());
    }

    @Test
    public void testUpdateUserLadderStatus_UserLevelsUp_ReturnsUpdatedDTO() {
        // Arrange
        testUser.setEarnedPoints(250); // Enough for level 2
        UserLadderStatus updatedStatus = new UserLadderStatus(testUser, level2, 250, 0);

        when(userService.getUserById(anyString())).thenReturn(testUser);
        when(userLadderStatusRepository.findById(anyString())).thenReturn(Optional.of(userLadderStatus));
        when(ladderLevelRepository.findAllByOrderByLevelAsc()).thenReturn(Arrays.asList(level1, level2));
        when(pointsCalculationStrategy.calculatePointsToNextLevel(anyInt(), any(), any())).thenReturn(0);
        when(userLadderStatusRepository.save(any(UserLadderStatus.class))).thenReturn(updatedStatus);

        // Act
        LadderStatusDTO result = ladderStatusService.updateUserLadderStatus("test-user-id");

        // Assert
        assertEquals(2, result.getCurrentLevel());
        assertEquals("Intermediate", result.getLevelLabel());
        assertEquals(250, result.getEarnedPoints());
        assertEquals(0, result.getPointsToNextLevel());
    }

    @Test
    public void testUpdateUserLadderStatus_UserDoesNotExist_ReturnsNull() {
        // Arrange
        when(userService.getUserById(anyString())).thenReturn(null);

        // Act
        LadderStatusDTO result = ladderStatusService.updateUserLadderStatus("non-existent-user");

        // Assert
        assertNull(result);
    }
}
