package com.franline.hispalismonument.dto;

import com.franline.hispalismonument.persistance.model.Monumento;
import com.franline.hispalismonument.persistance.model.Type;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MonumentoDTO {
    private Integer id;
    private String nombre;
    private String descripcionEs;
    private String descripcionEn;
    private String fotoUrl;
    private Double latitud;
    private Double longitud;
    private List<String> types;
    private String wikiPath;

    public MonumentoDTO(Monumento monumento){
        this.id = monumento.getId();
        this.nombre = monumento.getNombre();
        this.descripcionEs = monumento.getDescripcionEs();
        this.descripcionEn = monumento.getDescripcionEn();

        if (monumento.getFotoUrl() != null) {
            this.fotoUrl = monumento.getFotoUrl();
        }

        this.latitud = monumento.getLatitud();
        this.longitud = monumento.getLongitud();

        List<String > types = new ArrayList<>();
        for (Type type : monumento.getTypes()){
            types.add(type.getTypeName());
        }
        this.types = types;
        this.wikiPath = monumento.getWikiPath();
    }
}
