package com.safevision.authservice.repository;

import com.safevision.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities.
 * <p>
 * This interface implements the **Repository Pattern**, abstracting the underlying
 * data access logic (PostgreSQL). It provides methods for authentication lookups
 * and registration validation.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Finds a user by their unique username.
     * <p>
     * Critical for the {@code loadUserByUsername} method in Spring Security
     * during the login process.
     * </p>
     *
     * @param username The username to search for.
     * @return An {@link Optional} containing the user if found, or empty.
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email address.
     * Used for duplicate checks or alternative login methods.
     *
     * @param email The email address to search for.
     * @return An {@link Optional} containing the user if found.
     */
    Optional<User> findByEmail(String email);

    /**
     * efficient check to see if a username is already taken.
     * Used during the <b>Registration</b> process to fail fast.
     *
     * @param username The username to validate.
     * @return {@code true} if the user exists, {@code false} otherwise.
     */
    boolean existsByUsername(String username);

    /**
     * Efficient check to see if an email is already registered.
     * Used during the <b>Registration</b> process to ensure uniqueness.
     *
     * @param email The email to validate.
     * @return {@code true} if the email exists, {@code false} otherwise.
     */
    boolean existsByEmail(String email);
    
    boolean existsByEmailAndIdNot(String email, String id);
    
    
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, String id);
}