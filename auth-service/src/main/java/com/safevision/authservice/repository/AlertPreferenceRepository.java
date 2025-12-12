package com.safevision.authservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.safevision.authservice.model.AlertPreference;

public interface AlertPreferenceRepository extends JpaRepository<AlertPreference, String> {
    List<AlertPreference> findByUserId(String userId);
    void deleteByUserId(String userId);
}
