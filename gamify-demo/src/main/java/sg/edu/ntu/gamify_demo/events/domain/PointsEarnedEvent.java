package sg.edu.ntu.gamify_demo.events.domain;

import sg.edu.ntu.gamify_demo.models.User;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Event representing points being earned by a user.
 * This follows the Domain Events pattern to represent a significant occurrence in the domain.
 */
public class PointsEarnedEvent extends DomainEvent {
    private final int points;
    private final int newTotal;
    private final String source;
    private final JsonNode metadata;

    /**
     * Constructor for a points earned event.
     * 
     * @param user The user who earned the points.
     * @param points The number of points earned.
     * @param newTotal The new total points after earning.
     * @param source The source of the points (e.g., "TASK_COMPLETED").
     * @param metadata Additional data about the event.
     */
    public PointsEarnedEvent(User user, int points, int newTotal, String source, JsonNode metadata) {
        super("POINTS_EARNED", user);
        this.points = points;
        this.newTotal = newTotal;
        this.source = source;
        this.metadata = metadata;
    }

    /**
     * Get the number of points earned.
     * 
     * @return The points earned.
     */
    public int getPoints() {
        return points;
    }

    /**
     * Get the new total points after earning.
     * 
     * @return The new total points.
     */
    public int getNewTotal() {
        return newTotal;
    }

    /**
     * Get the source of the points.
     * 
     * @return The source.
     */
    public String getSource() {
        return source;
    }

    /**
     * Get additional metadata about the event.
     * 
     * @return The metadata.
     */
    public JsonNode getMetadata() {
        return metadata;
    }
}
