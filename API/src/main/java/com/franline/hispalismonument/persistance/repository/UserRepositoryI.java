package com.franline.hispalismonument.persistance.repository;

import com.franline.hispalismonument.persistance.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;


@Repository
public interface UserRepositoryI extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    @Query(value = "SELECT is_active_user(:userId, :minVisits)", nativeQuery = true)
    boolean isUserActive(@Param("userId") int userId, @Param("minVisits") int minVisits);
} 