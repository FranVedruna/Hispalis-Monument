package com.franline.hispalismonument.controller;

import com.franline.hispalismonument.dto.UserDTO;
import com.franline.hispalismonument.persistance.model.User;
import com.franline.hispalismonument.persistance.model.Rol;
import com.franline.hispalismonument.persistance.repository.UserRepositoryI;
import com.franline.hispalismonument.persistance.repository.RolRepositoryI;
import com.franline.hispalismonument.services.impl.ImageStorageService;
import com.franline.hispalismonument.services.impl.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final ImageStorageService imageStorageService;
    private final UserRepositoryI userRepository;
    private final RolRepositoryI rolRepositoryI;

    public UserController(UserService userService,
                          ImageStorageService imageStorageService,
                          UserRepositoryI userRepository,
                          RolRepositoryI rolRepositoryI) {
        this.userService = userService;
        this.imageStorageService = imageStorageService;
        this.userRepository = userRepository;
        this.rolRepositoryI = rolRepositoryI;
    }

    /**
     * Obtiene la información del usuario actualmente autenticado.
     */
    @GetMapping("/me")
    public UserDTO getCurrentUser(@AuthenticationPrincipal User user) {
        return userService.getUserById(user.getUserId());
    }

    /**
     * Obtiene una página de todos los usuarios registrados.
     */
    @GetMapping
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    /**
     * Busca usuarios por nombre, con soporte para paginación.
     */
    @GetMapping("/search")
    public Page<UserDTO> searchUsersByName(
            @RequestParam String nombre,
            Pageable pageable
    ) {
        return userService.findUsersByUsernameStarBy(nombre, pageable);
    }

    /**
     * Busca un usuario por nombre exacto y devuelve su información.
     */
    @GetMapping("/find")
    public ResponseEntity<UserDTO> getUserByName(@RequestParam String nombre) {
        try {
            UserDTO userDTO = userService.findUserByUsername(nombre);
            return ResponseEntity.ok(userDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Permite al usuario autenticado subir una foto de perfil.
     */
    @PostMapping("/me/photo")
    public ResponseEntity<String> uploadUserPhoto(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file) {

        try {
            String photoUrl = imageStorageService.store(file);
            user.setUserPhotoURL(photoUrl);
            userRepository.save(user);

            return ResponseEntity.ok(photoUrl);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    /**
     * Cambia el rol de un usuario específico al rol con ID 2.
     */
    @PutMapping("/upgrade")
    public ResponseEntity<String> changeUserRole(@RequestParam String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Rol newRole = rolRepositoryI.findById(2)
                    .orElseThrow(() -> new RuntimeException("Rol con ID 2 no encontrado"));

            user.setUserRol(newRole);
            userRepository.save(user);

            return ResponseEntity.ok("Rol actualizado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Elimina la cuenta del usuario autenticado.
     */
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteCurrentUser(@AuthenticationPrincipal User user) {
        try {
            userRepository.deleteById(user.getUserId());
            return ResponseEntity.ok("Usuario eliminado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el usuario: " + e.getMessage());
        }
    }

    /**
     * Devuelve la cantidad de monumentos visitados por un usuario.
     */
    @GetMapping("/visited/count")
    public ResponseEntity<Integer> getVisitedMonumentCount(@RequestParam String username) {
        try {
            int count = userService.getNumberMonumentVisited(username);
            return ResponseEntity.ok(count);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(0);
        }
    }

    /**
     * Verifica si un usuario está activo, basándose en si ha visitado al menos 7 monumentos.
     */
    @GetMapping("/{username}/is-active")
    public boolean isUserActive(@PathVariable String username) {
        int minVisits = 7; // número fijo de visitas

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return userService.checkIfUserIsActive(user.getUserId(), minVisits);
    }
}
