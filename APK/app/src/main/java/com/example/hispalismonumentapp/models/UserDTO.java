package com.example.hispalismonumentapp.models;

import java.util.List;

public class UserDTO {
    private int id;
    private String userName;
    private String userRol;
    private List<String> monuments;
    private String userBirthDate;
    private String userPhotoURL;

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserRol() {
        return userRol;
    }

    public void setUserRol(String userRol) {
        this.userRol = userRol;
    }

    public List<String> getMonuments() {
        return monuments;
    }

    public void setMonuments(List<String> monuments) {
        this.monuments = monuments;
    }

    public String getUserBirthDate() {
        return userBirthDate;
    }

    public void setUserBirthDate(String userBirthDate) {
        this.userBirthDate = userBirthDate;
    }

    public String getUserPhotoURL() {
        return userPhotoURL;
    }

    public void setUserPhotoURL(String userPhotoURL) {
        this.userPhotoURL = userPhotoURL;
    }

}