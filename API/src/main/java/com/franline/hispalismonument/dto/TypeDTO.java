package com.franline.hispalismonument.dto;

import com.franline.hispalismonument.persistance.model.Type;

public class TypeDTO {
    private String typeName;

    public TypeDTO() {

    }

    public TypeDTO(Type type) {
        this.typeName = type.getTypeName();
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}