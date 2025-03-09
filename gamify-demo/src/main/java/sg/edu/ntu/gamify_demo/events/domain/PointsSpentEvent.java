package sg.edu.ntu.gamify_demo.events.domain;

import sg.edu.ntu.gamify_demo.models.User;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Event representing points being spent by a user.
 * This follows the Domain Events pattern to represent a significant occurrence in the domain.
 */
public class PointsSpentEvent extends DomainEvent {
    private final int points;
    private final int newTotal;
    private final String source;
    private final JsonNode metadata;

    /**
     * Constructor for a points spent event.
     * 
     * @param user The user who spent the points.
     * @param points The number of points spent.
     * @param newTotal The new total available points after spending.
     * @param source The source of the spend (e.g., "REWARD_REDEMPTION").
     * @param metadata Additional data about the event.
     */
    public PointsSpentEvent(User user, int points, int newTotal, String source, JsonNode metadata) {
        super("POINTS_SPENT", user);
        this.points = points;
        this.newTotal = newTotal;
        this.source = source;
        this.metadata = metadata;
    }

    /**
     * Get the number of points spent.
     * 
     * @return The points spent.
     */
    public int getPoints() {
        return points;
    }

    /**
     * Get the new total available points after spending.
     * 
     * @return The new total available points.
     */
    public int getNewTotal() {
        return newTotal;
    }

    /**
     * Get the source of the spend.
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
