package com.safevision.authservice.service;

import java.util.Optional; // Note: You're using Optional here

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.safevision.authservice.model.User;
import com.safevision.authservice.repository.UserRepository;
import com.safevision.authservice.security.CustomJwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final CustomJwtUtil jwtUtil;
    
    // Explicit constructor (kept for robustness, though @RequiredArgsConstructor should cover it)
    public UserService(UserRepository repository, PasswordEncoder passwordEncoder, CustomJwtUtil jwtUtil) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public User createUser(User user) {
        // ASSUMPTION: The User model has a working getPassword() method.
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }

    public Optional<User> getUser(String id) {
        // Correct. CrudRepository.findById(ID) returns Optional<T>.
        return repository.findById(id); 
    }

    public User updateUser(User user) {
        // ASSUMPTION: The User model has a working getPassword() method.
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }

    public void deleteUser(String id) {
        // FIX 1: CrudRepository.delete() accepts the entity, or deleteById() accepts the ID.
        // We use deleteById(ID) which is the correct method for removing by ID.
        repository.deleteById(id); 
    }

    public String login(String id, String password) {
        // FIX 2 & 3: repository.findById(id) returns Optional<User>, not User.
        // We must handle the Optional before accessing the User object.
        Optional<User> userOptional = repository.findById(id);

        if (userOptional.isEmpty()) {
            return null;
        }
        
        // Retrieve the User object from the Optional
        User user = userOptional.get(); 

        // FIX 4: Assumes User model has a working getPassword() method.
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        return jwtUtil.generateToken(user.getId()); // Use user.getId() or username field for token subject
    }
}