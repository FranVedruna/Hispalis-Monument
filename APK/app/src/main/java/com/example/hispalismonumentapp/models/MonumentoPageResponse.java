package com.example.hispalismonumentapp.models;

import java.util.List;

public class MonumentoPageResponse {
    private List<MonumentoDTO> content;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;


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
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}