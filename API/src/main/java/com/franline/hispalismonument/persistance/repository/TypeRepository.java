package com.franline.hispalismonument.persistance.repository;


import com.franline.hispalismonument.persistance.model.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeRepository extends JpaRepository<Type, Integer> {
    Type findByTypeName(String typeName);
}
