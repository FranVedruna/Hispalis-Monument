package com.franline.hispalismonument.persistance.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.time.LocalDate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    Integer userId;

    @NotBlank(message = "El Username no puede estar vacío")
    @Size(min = 3, max = 20, message = "El nombre de usuario debe tener entre 3 y 20 caracteres")
    @Column(name = "username", nullable = false, unique = true)
    String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Roles_rol_id", referencedColumnName = "rol_id")
    Rol userRol;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, message = "La contraseña debe tener más de 8 caracteres")
    @Column(name = "password", nullable = false, columnDefinition = "CHAR(60)")
    String password;

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe ser válido")
    @Size(max = 90, message = "El email no puede tener más de 90 caracteres")
    @Column(name = "email")
    String email;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_visited_monuments",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "monument_id")
    )
    private List<Monumento> visitedMonuments = new ArrayList<>();


    @Column(name = "birth_date")
    private LocalDate userBirthDate;

    @Column(name = "photo")
    private String userPhotoURL;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("USER"));
    }

    /*TODO Mejorar los metodos de seguridad*/
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}