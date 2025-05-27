package com.franline.hispalismonument.services.impl;

import com.franline.hispalismonument.dto.UserDTO;
import com.franline.hispalismonument.persistance.model.User;
import com.franline.hispalismonument.persistance.repository.UserRepositoryI;
import com.franline.hispalismonument.services.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService implements IUserService {

    private final UserRepositoryI userRepository;
    private final ImageStorageService imageStorageService;

    public UserService(UserRepositoryI userRepository, ImageStorageService imageStorageService) {
        this.userRepository = userRepository;
        this.imageStorageService = imageStorageService;
    }

    /**
     * Obtiene un usuario por su ID y lo convierte en un DTO.
     *
     * @param userId ID del usuario a buscar.
     * @return UserDTO con los datos del usuario.
     * @throws RuntimeException si el usuario no existe.
     */
    public UserDTO getUserById(Integer userId) {
        User usuario = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return new UserDTO(usuario);
    }

    /**
     * Obtiene todos los usuarios paginados y los convierte en DTOs.
     *
     * @param pageable Información de paginación.
     * @return Página de UserDTOs.
     */
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserDTO::new);
    }

    /**
     * Busca un usuario por su nombre exacto de usuario.
     *
     * @param nombre Nombre de usuario.
     * @return UserDTO del usuario encontrado.
     * @throws RuntimeException si el usuario no existe.
     */
    public UserDTO findUserByUsername(String nombre) {
        User user = userRepository.findByUsername(nombre)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con nombre: " + nombre));
        return new UserDTO(user);
    }

    /**
     * Busca usuarios cuyo nombre contenga una cadena dada (ignorando mayúsculas/minúsculas).
     *
     * @param nombre Parte del nombre a buscar.
     * @param pageable Información de paginación.
     * @return Página de UserDTOs que coincidan con la búsqueda.
     */
    public Page<UserDTO> findUsersByUsernameStarBy(String nombre, Pageable pageable) {
        return userRepository.findByUsernameContainingIgnoreCase(nombre, pageable)
                .map(UserDTO::new);
    }

    /**
     * Actualiza la foto de perfil del usuario.
     *
     * @param user Usuario que desea actualizar su foto.
     * @param file Archivo de imagen.
     * @return URL de la imagen almacenada.
     * @throws RuntimeException en caso de error durante el almacenamiento.
     */
    public String updateUserPhoto(User user, MultipartFile file) {
        try {
            String photoUrl = imageStorageService.store(file);
            user.setUserPhotoURL(photoUrl);
            userRepository.save(user);
            return photoUrl;
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar la foto de usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Devuelve el número de monumentos visitados por un usuario.
     *
     * @param username Nombre de usuario.
     * @return Número de monumentos visitados.
     */
    public int getNumberMonumentVisited(String username) {
        return userRepository.countVisitedMonumentsByUsername(username);
    }

    /**
     * Verifica si un usuario es considerado activo en función de un número mínimo de visitas.
     *
     * @param userId ID del usuario.
     * @param minVisits Número mínimo de monumentos visitados para ser considerado activo.
     * @return true si el usuario es activo, false en caso contrario.
     */
    public boolean checkIfUserIsActive(int userId, int minVisits) {
        return userRepository.isUserActive(userId, minVisits);
    }
}