package com.franline.hispalismonument.services.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import com.franline.hispalismonument.services.IImageStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStorageService implements IImageStorageService {

    // Define la ruta de almacenamiento (ajusta la ruta según tus necesidades)
    private final Path storageLocation = Paths.get("src/main/resources/static/images");

    public ImageStorageService() {
        try {
            // Crea el directorio si no existe
            Files.createDirectories(storageLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de almacenamiento", e);
        }
    }

    /**
     * Almacena el archivo recibido y retorna la URL relativa.
     *
     * @param file MultipartFile recibido desde la petición
     * @return URL relativa de la imagen almacenada
     */
    public String store(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        try {
            Path targetLocation = storageLocation.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Retorna la URL relativa para acceder a la imagen
            return "/images/" + uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Fallo al almacenar la imagen " + originalFilename, e);
        }
    }
}
