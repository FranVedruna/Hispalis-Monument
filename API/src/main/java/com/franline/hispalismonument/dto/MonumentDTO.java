package com.franline.hispalismonument.dto;

import com.franline.hispalismonument.persistance.model.Monumento;
import com.franline.hispalismonument.persistance.model.Type;
import com.franline.hispalismonument.persistance.model.User;

import java.util.ArrayList;
import java.util.List;

public class MonumentDTO {
    int id;
    String name;
    String description;
    String wikiPath;
    String fotoURL;
    double latitud;
    double longitud;
    List<String> types;
    List<String> visitors;

    public MonumentDTO(Monumento monumento){
        this.id = monumento.getId();
        this.name = monumento.getNombre();
        this.description = monumento.getDescripcion();
        this.wikiPath = monumento.getWikiPath();
        this.fotoURL = monumento.getFotoUrl();
        this.latitud = monumento.getLatitud();
        this.longitud = monumento.getLongitud();

        List<String> typeList = new ArrayList<>();
        for (Type type : monumento.getTypes()){
            typeList.add(type.getTypeName());
        }
        this.types = typeList;

        List<String> visitors = new ArrayList<>();
        for (User user : monumento.getVisitors()){
            visitors.add(user.getUsername());
        }
        this.visitors = visitors;
    }
}
