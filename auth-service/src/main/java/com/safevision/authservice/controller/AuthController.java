package com.safevision.authservice.controller;

import com.safevision.authservice.model.User;
import com.safevision.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService service;
    
    public AuthController(UserService service) {
        this.service = service;
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        var created = service.createUser(user);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        String token = service.login(user.getId(), user.getPassword());
        if (token == null) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        return ResponseEntity.ok(new TokenResponse(token));
    }
    
    
    
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        return service.getUser(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody User user) {
        var updated = service.updateUser(user);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        service.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    // DTO for token response
    public static record TokenResponse(String token) {}
}
