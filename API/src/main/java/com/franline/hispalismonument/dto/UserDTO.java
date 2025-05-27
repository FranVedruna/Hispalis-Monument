package com.franline.hispalismonument.dto;

import com.franline.hispalismonument.persistance.model.Monumento;
import com.franline.hispalismonument.persistance.model.User;

import java.time.LocalDate;
import java.util.List;

public record UserDTO(
        Integer id,
        String userName,
        String userRol,
        List<String> monuments,
        LocalDate userBirthDate,
        String userPhotoURL
) {
    public UserDTO(User user) {
        this(
                user.getUserId(),
                user.getUsername(),
                user.getUserRol() != null ? user.getUserRol().getRolName() : "USER", // Valor por defecto
                user.getVisitedMonuments() != null ?
                        user.getVisitedMonuments().stream()
                                .map(Monumento::getNombre)
                                .toList() : List.of(), // Lista vacía si es null
                user.getUserBirthDate(),
                user.getUserPhotoURL()
        );
    }
}