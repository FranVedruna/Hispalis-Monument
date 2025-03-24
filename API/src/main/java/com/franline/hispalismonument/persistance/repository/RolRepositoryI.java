package com.franline.hispalismonument.persistance.repository;

import com.franline.hispalismonument.persistance.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface RolRepositoryI extends JpaRepository<Rol, Integer> {
    //https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
    Optional<Rol> findByRolName(String rolName);
} 
