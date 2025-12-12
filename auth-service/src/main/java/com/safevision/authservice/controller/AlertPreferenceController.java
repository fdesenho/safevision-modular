package com.safevision.authservice.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.safevision.authservice.service.AlertPreferenceService;
import com.safevision.common.enums.AlertType;

@RestController
@RequestMapping("/users")
public class AlertPreferenceController {

    private final AlertPreferenceService service;

    public AlertPreferenceController(AlertPreferenceService service) {
        this.service = service;
    }

    @GetMapping("/{userId}/alert-preferences")
    public List<AlertType> getPreferences(@PathVariable String userId) {
        return service.getPreferencesByUserId(userId);
    }
}
