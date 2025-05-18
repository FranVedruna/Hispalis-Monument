package com.example.hispalismonumentapp.models;

import java.util.List;

public class MonumentoPageResponse {
    private List<MonumentoDTO> content;
    private int totalPages;
    private long totalElements;
    private int number;  // currentPage
    private int size;    // pageSize
    private boolean first;
    private boolean last;

    // Getters y setters
    public List<MonumentoDTO> getContent() {
        return content;
    }

    public void setContent(List<MonumentoDTO> content) {
        this.content = content;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getCurrentPage() {
        return number;
    }

    public int getPageSize() {
        return size;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }

    // Métodos adicionales útiles
    public boolean hasNext() {
        return !isLast();
    }

    public boolean hasPrevious() {
        return !isFirst();
    }
}