package com.safevision.authservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.safevision.authservice.model.AlertPreference;
import com.safevision.authservice.repository.AlertPreferenceRepository;
import com.safevision.common.enums.AlertType;

@Service
public class AlertPreferenceService {

    private final AlertPreferenceRepository repository;

    public AlertPreferenceService(AlertPreferenceRepository repository) {
        this.repository = repository;
    }

    public List<AlertType> getPreferencesByUserId(String userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(AlertPreference::getAlertType)
                .collect(Collectors.toList());
    }
}

