package com.franline.hispalismonument.controller;

import com.franline.hispalismonument.dto.MonumentoDTO;
import com.franline.hispalismonument.persistance.model.Monumento;
import com.franline.hispalismonument.persistance.model.User;
import com.franline.hispalismonument.services.impl.MonumentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    /**
     * Obtiene una lista paginada de todos los monumentos.
     */
    @GetMapping
    public Page<MonumentoDTO> getAllMonumentos(Pageable pageable) {
        return monumentoService.searchAll(pageable);
    }

    /**
     * Busca un monumento por su nombre exacto.
     */
    @GetMapping("/buscar")
    public MonumentoDTO getMonumentoByName(@RequestParam String nombre) {
        return monumentoService.searchMonumentByName(nombre);
    }

    /**
     * Elimina un monumento por su nombre.
     * Retorna un mensaje de éxito o error en caso de que no se encuentre.
     */
    @DeleteMapping("/eliminar")
    public ResponseEntity<String> eliminarMonumentoPorNombre(@RequestParam String nombre) {
        try {
            monumentoService.deleteMonumentByName(nombre);
            return ResponseEntity.ok("Monumento eliminado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }
    }

    /**
     * Busca un monumento por su ID.
     */
    @GetMapping("/{id}")
    public MonumentoDTO getMonumentoById(@PathVariable int id) {
        return monumentoService.searchMonumentById(id);
    }

    /**
     * Crea un nuevo monumento. Se puede incluir una imagen opcional.
     * El monumento y la imagen se reciben como partes separadas en la solicitud multipart.
     */
    @PostMapping("/crear")
    public ResponseEntity<Monumento> createMonumento(
            @RequestPart("monumento") Monumento monumento,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        System.out.println(monumento);
        Monumento savedMonumento = monumentoService.createOrUpdateMonumento(monumento, image);
        return new ResponseEntity<>(savedMonumento, HttpStatus.CREATED);
    }

    /**
     * Verifica si el usuario autenticado ha visitado un monumento específico por su nombre.
     */
    @GetMapping("/visitado/{nombre}")
    public ResponseEntity<Boolean> haVisitadoMonumento(
            @PathVariable String nombre,
            @AuthenticationPrincipal User user) {

        boolean visitado = monumentoService.getMonumentIsVisited(user.getUsername(), nombre);
        return ResponseEntity.ok(visitado);
    }

    /**
     * Registra que el usuario autenticado ha visitado un monumento por su nombre.
     * Si ya lo ha visitado, se devuelve un error.
     */
    @PostMapping("/visitado/{nombre}")
    public ResponseEntity<String> agregarVisita(
            @PathVariable String nombre,
            @AuthenticationPrincipal User user) {

        try {
            monumentoService.setMonumentIsVisited(user.getUsername(), nombre);
            return ResponseEntity.status(HttpStatus.CREATED).body("Visita añadida con éxito.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Busca monumentos por coincidencias parciales en el nombre.
     * Soporta paginación.
     */
    @GetMapping("/buscar/partial")
    public Page<MonumentoDTO> searchMonumentsByPartialName(
            @RequestParam String nombre,
            Pageable pageable) {
        return monumentoService.searchMonumentsByPartialName(nombre, pageable);
    }
}
