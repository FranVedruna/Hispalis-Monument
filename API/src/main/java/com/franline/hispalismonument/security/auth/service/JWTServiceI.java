package com.franline.hispalismonument.security.auth.service;

import java.security.Key;
import java.util.Map;

import com.franline.hispalismonument.persistance.model.User;
import org.springframework.security.core.userdetails.UserDetails;


public interface JWTServiceI {
    String getToken(User user);

    String getToken(Map<String, Object> extraClaims, User user);

    Key getKey();

    String getUsernameFromToken(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
}
