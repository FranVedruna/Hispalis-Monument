package com.example.hispalismonumentapp.models;

public class TypeDTO {
    private String typeName;

    public TypeDTO() {}

    public TypeDTO(String typeName){
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}