package com.example.hispalismonumentapp.models;

import java.util.ArrayList;
import java.util.List;

public class Monument {
    private int id;
    private String nombre;
    private String descripcion;
    private String fotoUrl;
    private Double latitud;  // Cambiado a Double para permitir null
    private Double longitud; // Cambiado a Double para permitir null
    private List<String> types = new ArrayList<>(); // Inicializado por defecto
    private String wikiPath = ""; // Valor por defecto

    // Constructor mejorado que maneja nulls
    public Monument(Integer id, String nombre, String descripcion, String fotoUrl,
                    Double latitud, Double longitud, List<String> types, String wikiPath) {
        this.id = id != null ? id : 0;
        this.nombre = nombre != null ? nombre : "";
        this.descripcion = descripcion != null ? descripcion : "";
        this.fotoUrl = fotoUrl != null ? fotoUrl : "";
        this.latitud = latitud != null ? latitud : 0.0;
        this.longitud = longitud != null ? longitud : 0.0;
        this.types = types != null ? types : new ArrayList<>();
        this.wikiPath = wikiPath != null ? wikiPath : "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public String getWikiPath() {
        return wikiPath;
    }

    public void setWikiPath(String wikiPath) {
        this.wikiPath = wikiPath;
    }
}