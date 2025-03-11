package sg.edu.ntu.gamify_demo.models;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The PointsTransaction class represents a transaction that affects a user's points.
 * It records point earnings and redemptions with metadata about the event.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "points_transactions")
public class PointsTransaction {
    @Id
    @Column(name = "transaction_id")
    private String transactionId;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "event_type", nullable = false)
    private String eventType;
    
    @Column(name = "points", nullable = false)
    private int points;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "json")
    private JsonNode metadata;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * Constructs a PointsTransaction with the provided details.
     * 
     * @param user The user associated with this transaction.
     * @param eventType The type of event that triggered this transaction.
     * @param points The number of points (positive for earnings, negative for redemptions).
     * @param metadata Additional data about the transaction.
     */
    public PointsTransaction(User user, String eventType, int points, JsonNode metadata) {
        this.transactionId = UUID.randomUUID().toString();
        this.user = user;
        this.eventType = eventType;
        this.points = points;
        this.timestamp = LocalDateTime.now();
        this.metadata = metadata;
    }
}
