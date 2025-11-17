package com.safevision.alertservice.repository;

import com.safevision.alertservice.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, String> {
    List<Alert> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Alert> findByUserIdAndAcknowledgedFalseOrderByCreatedAtDesc(String userId);
}
