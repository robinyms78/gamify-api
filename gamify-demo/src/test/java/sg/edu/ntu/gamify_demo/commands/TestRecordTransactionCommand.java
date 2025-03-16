package sg.edu.ntu.gamify_demo.commands;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.models.PointsTransaction;
import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.repositories.PointsTransactionRepository;

/**
 * Test-specific implementation of RecordTransactionCommand that doesn't publish events.
 * This is used to avoid issues with the event system in tests.
 */
public class TestRecordTransactionCommand implements TaskEventCommand {
    
    private final User user;
    private final String taskId;
    private final JsonNode eventData;
    private final Long points;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor for the command.
     * 
     * @param user The user executing the command.
     * @param taskId The ID of the task.
     * @param eventData Additional data about the event.
     * @param points The points to record.
     * @param pointsTransactionRepository Repository for points transactions.
     * @param objectMapper JSON object mapper.
     */
    public TestRecordTransactionCommand(
            User user,
            String taskId,
            JsonNode eventData,
            Long points,
            PointsTransactionRepository pointsTransactionRepository,
            ObjectMapper objectMapper) {
        this.user = user;
        this.taskId = taskId;
        this.eventData = eventData;
        this.points = points;
        this.pointsTransactionRepository = pointsTransactionRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Execute the command to record a points transaction.
     * 
     * @return A task event (null in this case, as we're not creating a task event).
     */
    @Override
    public TaskEvent execute() {
        // Create metadata with task information
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("taskId", taskId);
        if (eventData != null) {
            metadata.set("eventData", eventData);
        }
        
        // Create and save the points transaction
        PointsTransaction transaction = new PointsTransaction(
                user, 
                "TASK_COMPLETED", 
                points, 
                metadata);
        transaction.setCreatedAt(ZonedDateTime.now());
        
        // Save the transaction
        PointsTransaction savedTransaction = pointsTransactionRepository.save(transaction);
        
        // Update user's points
        user.addPointsTransaction(savedTransaction);
        
        // Return null as we're not creating a task event
        return null;
    }
}
