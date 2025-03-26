package com.franline.hispalismonument.persistance.repository;

import com.franline.hispalismonument.persistance.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;


@Repository
public interface UserRepositoryI extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Page<User> findByUsernameStartingWith(String name);
} 