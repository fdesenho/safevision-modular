package com.safevision.authservice.repository;

import com.safevision.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // findById is inherited from JpaRepository
}
