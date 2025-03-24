package com.franline.hispalismonument.persistance.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


/***
 * Entidad ROL
 * Variables:
 *     - ID (Integer, autoincremental, not-null)
 *     - RolName (String) - Nombre del rol (USER/ADMIN)
 *     - UserWithThisRol (List<User>) @OneToMany - Lista de usuarios que usa un rol específico
 */

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "roles")
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_id", nullable = false)
    Integer rolId;

    @NotBlank(message = "El nombre del rol no puede estar vacío")
    @Size(max = 45, message = "El nombre del rol no puede tener más de 45 caracteres")
    @Column(name = "rol_name")
    String rolName;

    @OneToMany(mappedBy = "userRol", 
               cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<User> usersWithThisRol;
}
