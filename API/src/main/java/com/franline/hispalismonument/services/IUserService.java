package com.franline.hispalismonument.services;

import com.franline.hispalismonument.dto.UserDTO;
import com.franline.hispalismonument.persistance.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {
    public UserDTO getUserById(Integer userId);
    public Page<UserDTO> getAllUsers(Pageable pageable);
    public UserDTO findUserByUsername(String nombre);
    public Page<UserDTO> findUsersByUsernameStarBy(String nombre, Pageable pageable);
    public String updateUserPhoto(User user, MultipartFile file);
    public int getNumberMonumentVisited(String username);
    public boolean checkIfUserIsActive(int userId, int minVisits);
}
