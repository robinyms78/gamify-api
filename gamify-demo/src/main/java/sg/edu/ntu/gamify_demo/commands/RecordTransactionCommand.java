package sg.edu.ntu.gamify_demo.commands;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.Services.PointsService;
import sg.edu.ntu.gamify_demo.events.domain.DomainEventPublisher;
import sg.edu.ntu.gamify_demo.events.domain.PointsEarnedEvent;
import sg.edu.ntu.gamify_demo.models.PointsTransaction;
import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.repositories.PointsTransactionRepository;

/**
 * Command to record a points transaction for a completed task.
 * This follows the Command pattern to encapsulate the transaction recording logic.
 */
public class RecordTransactionCommand implements TaskEventCommand {
    
    private final User user;
    private final String taskId;
    private final JsonNode eventData;
    private final int points;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final PointsService pointsService;
    private final DomainEventPublisher domainEventPublisher;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor for the command.
     * 
     * @param user The user executing the command.
     * @param taskId The ID of the task.
     * @param eventData Additional data about the event.
     * @param points The points to record.
     * @param pointsTransactionRepository Repository for points transactions.
     * @param pointsService Service for managing points.
     * @param domainEventPublisher Publisher for domain events.
     * @param objectMapper JSON object mapper.
     */
    public RecordTransactionCommand(
            User user,
            String taskId,
            JsonNode eventData,
            int points,
            PointsTransactionRepository pointsTransactionRepository,
            PointsService pointsService,
            DomainEventPublisher domainEventPublisher,
            ObjectMapper objectMapper) {
        this.user = user;
        this.taskId = taskId;
        this.eventData = eventData;
        this.points = points;
        this.pointsTransactionRepository = pointsTransactionRepository;
        this.pointsService = pointsService;
        this.domainEventPublisher = domainEventPublisher;
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
        
        // Save the transaction
        PointsTransaction savedTransaction = pointsTransactionRepository.save(transaction);
        
        // Update user's points
        user.addPointsTransaction(savedTransaction);
        
        // Publish a domain event for points earned
        int newTotal = user.getAvailablePoints();
        PointsEarnedEvent pointsEarnedEvent = new PointsEarnedEvent(
                user, points, newTotal, "TASK_COMPLETED", eventData);
        domainEventPublisher.publish(pointsEarnedEvent);
        
        // Return null as we're not creating a task event
        return null;
    }
}
