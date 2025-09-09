package com.project.demo.repository;

import com.project.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // This method will automatically be implemented by Spring Data JPA
    Optional<User> findByUsername(String username);
}
