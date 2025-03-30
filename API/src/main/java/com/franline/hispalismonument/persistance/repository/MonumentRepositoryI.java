package com.franline.hispalismonument.persistance.repository;

import com.franline.hispalismonument.persistance.model.Monumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonumentRepositoryI extends JpaRepository<Monumento, Integer> {

    @Query("SELECT m FROM Monumento m JOIN m.types t WHERE t.typeName = :nombreTipo")
    List<Monumento> findMonumentsByTypeName(@Param("nombreTipo") String nombreTipo);

    Optional<Monumento> findByNombre(String nombre);
}