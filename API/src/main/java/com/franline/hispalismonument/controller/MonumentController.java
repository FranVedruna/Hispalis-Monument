package com.franline.hispalismonument.controller;

import com.franline.hispalismonument.dto.MonumentoDTO;
import com.franline.hispalismonument.persistance.model.Monumento;
import com.franline.hispalismonument.services.MonumentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/monumentos")
public class MonumentController {

    private final MonumentoService monumentoService;

    @Autowired
    public MonumentController(MonumentoService monumentoService) {
        this.monumentoService = monumentoService;
    }


    @GetMapping
    public Page<MonumentoDTO> getAllMonumentos(Pageable pageable) {
        return monumentoService.searchAll(pageable);
    }

    @GetMapping("/buscar")
    public MonumentoDTO getMonumentoByName(@RequestParam String nombre) {
        Monumento monumento = monumentoService.searchMonumentByName(nombre);
        return new MonumentoDTO(monumento);
    }

    @GetMapping("/{id}")
    public MonumentoDTO getMonumentoById(@PathVariable int id) {
        return monumentoService.searchMonumentById(id);
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
