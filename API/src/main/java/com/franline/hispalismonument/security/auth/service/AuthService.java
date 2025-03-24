package com.franline.hispalismonument.security.auth.service;
import com.franline.hispalismonument.persistance.model.Rol;
import com.franline.hispalismonument.persistance.model.User;
import com.franline.hispalismonument.persistance.repository.RolRepositoryI;
import com.franline.hispalismonument.persistance.repository.UserRepositoryI;
import com.franline.hispalismonument.security.auth.dto.AuthResponseDTO;
import com.franline.hispalismonument.security.auth.dto.LoginRequestDTO;
import com.franline.hispalismonument.security.auth.dto.RegisterRequestDTO;
import com.franline.hispalismonument.security.auth.service.AuthServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalTime;

@Service
public class AuthService implements AuthServiceI {

    @Autowired
    private UserRepositoryI userRepo;

    @Autowired
    private RolRepositoryI rolRepo;


    @Autowired
    private JWTServiceImpl jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getName(), request.getPassword()));
        User user=userRepo.findByUsername(request.getName()).orElseThrow();
        return new AuthResponseDTO(jwtService.getToken(user));
    }

    public void register(RegisterRequestDTO request) {
        Rol rol = rolRepo.findByRolName("USER")
                .orElseThrow(() -> new RuntimeException("Rol not found"));
        User user = new User();
        user.setUsername(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setUserRol(rol);
        userRepo.save(user);
    }
}
