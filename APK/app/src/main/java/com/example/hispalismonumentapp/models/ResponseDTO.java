package com.example.hispalismonumentapp.models;

public class ResponseDTO {
    private String message;

    public ResponseDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}