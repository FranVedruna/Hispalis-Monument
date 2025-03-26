package com.franline.hispalismonument.persistance.repository;

import com.franline.hispalismonument.persistance.model.Monumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MonumentRepositoryI extends JpaRepository<Monumento, Integer> {

    // Opción: Buscar monumentos cuyo tipo esté en la lista de tipos
    @Query("SELECT m FROM Monumento m JOIN m.types t WHERE t.nombre = :nombreTipo")
    List<Monumento> findMonumentsByTypeName(@Param("nombreTipo") String nombreTipo);
}