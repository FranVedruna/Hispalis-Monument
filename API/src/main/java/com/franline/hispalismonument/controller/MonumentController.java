package com.franline.hispalismonument.controller;

import com.franline.hispalismonument.persistance.model.Monumento;
import com.franline.hispalismonument.services.MonumentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/monumentos")
public class MonumentController {

    private final MonumentoService monumentoService;

    @Autowired
    public MonumentController(MonumentoService monumentoService) {
        this.monumentoService = monumentoService;
    }




    @PostMapping("/crear")
    public ResponseEntity<Monumento> createMonumento(
            @RequestPart("monumento") Monumento monumento,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        Monumento savedMonumento = monumentoService.createOrUpdateMonumento(monumento, image);
        return new ResponseEntity<>(savedMonumento, HttpStatus.CREATED);
    }

    // Puedes agregar otros endpoints para actualizar, obtener, eliminar, etc.
}
