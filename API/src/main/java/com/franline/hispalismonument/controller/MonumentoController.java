package com.franline.hispalismonument.controller;

import com.franline.hispalismonument.dto.MonumentoDTO;
import com.franline.hispalismonument.persistance.model.Monumento;
import com.franline.hispalismonument.services.MonumentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


@RestController
@RequestMapping("/monumentos")
public class MonumentoController {

/*
    private static final String UPLOAD_DIR = "src/main/resources/static/images/";

    @GetMapping
    public Monumento obtenerMonumento() {
        return new Monumento(
                "La Giralda",
                "Torre campanario de la Catedral de Sevilla, famosa por su historia y arquitectura.",
                "giralda.jpg" // Solo el nombre del archivo
        );
    }
    @PostMapping("/subir")
    public ResponseEntity<String> subirMonumento(
            @RequestParam("titulo") String titulo,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("imagen") MultipartFile imagen) {

        try {
            // Reemplazar espacios y caracteres especiales en el título para el nombre del archivo
            String nombreArchivo = titulo.replaceAll("[^a-zA-Z0-9.-]", "_") + ".jpg";

            // Definir la ruta donde se guardará la imagen
            String filePath = UPLOAD_DIR + nombreArchivo;
            Files.write(Paths.get(filePath), imagen.getBytes());

            // Aquí puedes guardar los datos en la base de datos (opcional)
            System.out.println("Título: " + titulo);
            System.out.println("Descripción: " + descripcion);
            System.out.println("Imagen guardada en: " + filePath);

            return ResponseEntity.ok("Monumento subido correctamente");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al subir el monumento");
        }
    }

 */
}