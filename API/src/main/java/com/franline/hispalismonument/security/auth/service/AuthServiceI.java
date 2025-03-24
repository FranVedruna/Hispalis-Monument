package com.franline.hispalismonument.security.auth.service;


import com.franline.hispalismonument.security.auth.dto.AuthResponseDTO;
import com.franline.hispalismonument.security.auth.dto.LoginRequestDTO;
import com.franline.hispalismonument.security.auth.dto.RegisterRequestDTO;

public interface AuthServiceI {
    AuthResponseDTO login(LoginRequestDTO request);
    void register(RegisterRequestDTO request);
}
