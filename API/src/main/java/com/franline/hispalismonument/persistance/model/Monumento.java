package com.franline.hispalismonument.persistance.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "monuments")
public class Monumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "monument_id", nullable = false)
    private Integer id;

    @NotBlank(message = "El monumento debe tener un nombre")
    @Size(max = 100, message = "El nombre debe tener menos de 100 caracteres")
    @Column(name = "name", nullable = false, unique = true)
    private String nombre;

    @NotBlank(message = "El monumento debe tener una descripción")
    @Column(name = "description", nullable = false)
    private String descripcion;

    @Column(name = "photo")
    private String fotoUrl;

    @Column(name = "lat", nullable = false)
    private Double latitud;

    @Column(name = "lon", nullable = false)
    private Double longitud;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "monument_types",
            joinColumns = @JoinColumn(name = "monument_id"),
            inverseJoinColumns = @JoinColumn(name = "type_id")
    )
    private List<Type> types;


    @ManyToMany(mappedBy = "visitedMonuments")
    private List<User> visitors = new ArrayList<>();


    @Column(name = "wiki")
    private String wikiPath;
}
