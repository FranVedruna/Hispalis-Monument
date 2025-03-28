package com.franline.hispalismonument.dto;

import com.franline.hispalismonument.persistance.model.Monumento;
import com.franline.hispalismonument.persistance.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserDTO {
    int id;
    String userName;
    String userRol;
    List<String> monuments;
    LocalDate userBirthDate;
    String userPhotoURL;

    public UserDTO(User user){
        this.id = user.getUserId();
        this.userName = user.getUsername();
        this.userRol = user.getUserRol().getRolName();
        this.userBirthDate = user.getUserBirthDate();
        this.userPhotoURL = user.getUserPhotoURL();

        List<String> monuments = new ArrayList<>();
        for (Monumento monumento : user.getVisitedMonuments()){
            monuments.add(monumento.getNombre());
        }

        this.monuments = monuments;
    }
}
