package com.franline.hispalismonument.security.auth.controller;

import com.franline.hispalismonument.dto.ResponseDTO;
import com.franline.hispalismonument.security.auth.dto.AuthResponseDTO;
import com.franline.hispalismonument.security.auth.dto.LoginRequestDTO;
import com.franline.hispalismonument.security.auth.dto.RegisterRequestDTO;
import com.franline.hispalismonument.security.auth.service.AuthServiceI;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthServiceI authService;

    @PostMapping(value = "/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping(value = "/register")
    public ResponseEntity<ResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        authService.register(request);
        return ResponseEntity.ok(new ResponseDTO("User registered successfully"));
    }

}
