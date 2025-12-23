package com.safevision.alertservice.client;

import com.safevision.common.enums.AlertType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collections;
import java.util.List;

@FeignClient(
    name = "auth-service", 
    path = "/auth",
    fallback = AlertPreferenceClientFallback.class
)
public interface AlertPreferenceClient {

    @GetMapping("/alert-preferences/{username}")
    List<AlertType> getPreferences(@PathVariable("username") String username);
}

@Component
class AlertPreferenceClientFallback implements AlertPreferenceClient {
    @Override
    public List<AlertType> getPreferences(String username) {
        return Collections.emptyList();
    }
}