package com.example.hispalismonumentapp.models;

import java.util.List;

public class UserPageResponse {
    private List<UserDTO> content;
    private int totalPages;

    public List<UserDTO> getContent() {
        return content;
    }

    public int getTotalPages() {
        return totalPages;
    }
}

