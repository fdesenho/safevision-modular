package com.safevision.alertservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.safevision.common.enums.AlertType;
import java.util.List;


@FeignClient(name = "auth-service", path = "/auth")
public interface AlertPreferenceClient {

    
    @GetMapping("/alert-preferences/{username}")
    List<AlertType> getPreferences(@PathVariable("username") String username);
}