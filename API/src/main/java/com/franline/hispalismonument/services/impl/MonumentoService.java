package com.franline.hispalismonument.services.impl;

import com.franline.hispalismonument.dto.MonumentoDTO;
import com.franline.hispalismonument.persistance.model.Monumento;
import com.franline.hispalismonument.persistance.model.Type;
import com.franline.hispalismonument.persistance.model.User;
import com.franline.hispalismonument.persistance.repository.MonumentRepositoryI;
import com.franline.hispalismonument.persistance.repository.TypeRepository;
import com.franline.hispalismonument.persistance.repository.UserRepositoryI;
import com.franline.hispalismonument.services.IMonumentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Service
public class MonumentoService implements IMonumentService {

    private final UserRepositoryI userRepository;
    private final MonumentRepositoryI monumentoRepository;
    private final ImageStorageService imageStorageService;
    private final TypeRepository typeRepository;

    @Autowired
    public MonumentoService(UserRepositoryI userRepository,
                            MonumentRepositoryI monumentoRepository,
                            ImageStorageService imageStorageService,
                            TypeRepository typeRepository) {
        this.userRepository = userRepository;
        this.monumentoRepository = monumentoRepository;
        this.imageStorageService = imageStorageService;
        this.typeRepository = typeRepository;
    }

    /**
     * Crea o actualiza un monumento, incluyendo la imagen y validación de tipos.
     *
     * @param monumento   Objeto Monumento a guardar.
     * @param imageFile   Imagen opcional a asociar.
     * @return Monumento guardado.
     * @throws IllegalArgumentException si los tipos tienen nombres nulos o vacíos.
     * @throws RuntimeException si algún tipo no se encuentra en la base de datos.
     */
    public Monumento createOrUpdateMonumento(Monumento monumento, MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = imageStorageService.store(imageFile);
            monumento.setFotoUrl(imageUrl);
        }

        if (monumento.getTypes() != null) {
            List<Type> managedTypes = new ArrayList<>();

            for (Type type : monumento.getTypes()) {
                if (type.getTypeName() == null) {
                    throw new IllegalArgumentException("El nombre del tipo no puede ser nulo");
                } else if (type.getTypeName().isEmpty()) {
                    throw new IllegalArgumentException("El nombre del tipo no puede estar vacío");
                }

                Type existingType = typeRepository.findByTypeName(type.getTypeName());
                if (existingType == null) {
                    throw new RuntimeException("Tipo no encontrado: " + type.getTypeName());
                }
                managedTypes.add(existingType);
            }

            monumento.setTypes(managedTypes);
        }
        return monumentoRepository.save(monumento);
    }

    /**
     * Elimina un monumento por nombre, asegurándose de limpiar la relación con usuarios.
     *
     * @param nombre Nombre del monumento a eliminar.
     * @throws RuntimeException si el monumento no se encuentra.
     */
    @Transactional
    public void deleteMonumentByName(String nombre) {
        Monumento monumento = monumentoRepository.findByNombre(nombre)
                .orElseThrow(() -> new RuntimeException("Monumento no encontrado"));

        // Elimina las referencias a este monumento en los usuarios
        List<User> visitantes = userRepository.findAll();
        for (User user : visitantes) {
            user.getVisitedMonuments().remove(monumento);
        }

        monumentoRepository.delete(monumento);
    }

    /**
     * Verifica si un usuario ha visitado un monumento específico.
     *
     * @param username     Nombre del usuario.
     * @param monumentName Nombre del monumento.
     * @return true si el usuario lo ha visitado, false si no.
     */
    public boolean getMonumentIsVisited(String username, String monumentName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        Monumento monumento = monumentoRepository.findByNombre(monumentName)
                .orElseThrow(() -> new RuntimeException("Monumento no encontrado"));

        return user.getVisitedMonuments()
                .stream()
                .anyMatch(m -> m.getNombre().equalsIgnoreCase(monumento.getNombre()));
    }

    /**
     * Busca un monumento por nombre exacto.
     *
     * @param monumentName Nombre del monumento.
     * @return DTO del monumento encontrado.
     * @throws RuntimeException si el monumento no se encuentra.
     */
    public MonumentoDTO searchMonumentByName(String monumentName) {
        return monumentoRepository.findByNombre(monumentName)
                .map(MonumentoDTO::new)
                .orElseThrow(() -> new RuntimeException("Monumento no encontrado"));
    }

    /**
     * Busca un monumento por ID.
     *
     * @param id ID del monumento.
     * @return DTO del monumento encontrado.
     * @throws RuntimeException si no se encuentra.
     */
    public MonumentoDTO searchMonumentById(int id) {
        return monumentoRepository.findById(id)
                .map(MonumentoDTO::new)
                .orElseThrow(() -> new RuntimeException("Monumento no encontrado"));
    }

    /**
     * Marca un monumento como visitado por un usuario.
     *
     * @param username     Nombre del usuario.
     * @param monumentName Nombre del monumento.
     * @throws RuntimeException si el usuario ya lo ha visitado o no se encuentra.
     */
    public void setMonumentIsVisited(String username, String monumentName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        Monumento monumento = monumentoRepository.findByNombre(monumentName)
                .orElseThrow(() -> new RuntimeException("Monumento no encontrado"));

        if (user.getVisitedMonuments().contains(monumento)) {
            throw new RuntimeException("El usuario ya ha visitado este monumento.");
        }

        user.getVisitedMonuments().add(monumento);
        userRepository.save(user);
    }

    /**
     * Devuelve todos los monumentos paginados en formato DTO.
     *
     * @param pageable Configuración de paginación.
     * @return Página de DTOs de monumentos.
     */
    public Page<MonumentoDTO> searchAll(Pageable pageable) {
        List<Monumento> allMonuments = monumentoRepository.findAll();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allMonuments.size());
        List<Monumento> pageList = allMonuments.subList(start, end);

        return new PageImpl<>(pageList, pageable, allMonuments.size()).map(MonumentoDTO::new);
    }

    /**
     * Busca monumentos cuyo nombre contenga una cadena parcial (ignorando mayúsculas).
     *
     * @param nombreParcial Parte del nombre.
     * @param pageable Configuración de paginación.
     * @return Página de DTOs de monumentos coincidentes.
     */
    public Page<MonumentoDTO> searchMonumentsByPartialName(String nombreParcial, Pageable pageable) {
        Page<Monumento> monumentsPage = monumentoRepository.findByNombreContainingIgnoreCase(nombreParcial, pageable);
        return monumentsPage.map(MonumentoDTO::new);
    }
}