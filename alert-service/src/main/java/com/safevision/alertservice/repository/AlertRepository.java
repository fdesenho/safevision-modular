package com.safevision.alertservice.repository;

import com.safevision.alertservice.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing {@link Alert} entities in the PostgreSQL database.
 * <p>
 * Extends JpaRepository to provide standard CRUD operations.
 * Custom finder methods are automatically implemented by Spring Data based on method names.
 * </p>
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, String> {

    /**
     * Finds all alerts belonging to a specific user, sorted by creation date (newest first).
     * Used for populating the user's dashboard history.
     *
     * @param userId The UUID of the user.
     * @return A list of alerts.
     */
    List<Alert> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Finds only unacknowledged (unread) alerts for a specific user, sorted by newest first.
     * Useful for showing active notifications or badges.
     *
     * @param userId The UUID of the user.
     * @return A list of unread alerts.
     */
    List<Alert> findByUserIdAndAcknowledgedFalseOrderByCreatedAtDesc(String userId);
}