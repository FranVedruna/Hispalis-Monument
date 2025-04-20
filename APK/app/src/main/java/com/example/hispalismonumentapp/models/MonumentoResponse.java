package com.example.hispalismonumentapp.models;

import java.util.List;

public class MonumentoResponse {
    private List<MonumentoDTO> content;
    private int totalPages;
    private long totalElements;

    // Getters y setters
    public List<MonumentoDTO> getContent() {
        return content;
    }
}