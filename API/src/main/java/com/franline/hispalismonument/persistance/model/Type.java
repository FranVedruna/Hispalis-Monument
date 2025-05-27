package com.franline.hispalismonument.persistance.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "types")
public class Type {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id", nullable = false)
    private Integer typeId;

    @NotBlank(message = "El nombre del tipo de monumento no puede estar vacío")
    @Size(max = 45, message = "El nombre del tipo de monumento no puede tener más de 45 caracteres")
    @Column(name = "type_name", nullable = false, unique = true)
    private String typeName;

    @ManyToMany(mappedBy = "types", fetch = FetchType.LAZY)
    private List<Monumento> monumentos;

    @Override
    public String toString() {
        return "Type{" +
                "typeName='" + typeName + '\'' +
                '}';
    }
}
