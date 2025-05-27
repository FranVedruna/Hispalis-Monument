package com.franline.hispalismonument.controller;

import com.franline.hispalismonument.dto.TypeDTO;
import com.franline.hispalismonument.services.impl.TypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/type")
public class TypeController {

    private final TypeService typeService;

    @Autowired
    public TypeController(TypeService typeService) {
        this.typeService = typeService;
    }

    /**
     * Devuelve una lista con todos los Types existentes.
     */
    @GetMapping
    public ResponseEntity<List<TypeDTO>> getAllTypes() {
        List<TypeDTO> types = typeService.getAllTypes()
                .stream()
                .map(type -> {
                    TypeDTO dto = new TypeDTO();
                    dto.setTypeName(type.getTypeName());
                    return dto;
                })
                .collect(Collectors.toList());
        return new ResponseEntity<>(types, HttpStatus.OK);
    }
}