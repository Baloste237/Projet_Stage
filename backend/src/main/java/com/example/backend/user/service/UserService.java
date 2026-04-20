package com.example.backend.user.service;

import com.example.backend.user.dto.UserRequest;
import com.example.backend.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse createUser(UserRequest request);
    UserResponse updateUser(Long id, UserRequest request);
    void deleteUser(Long id);
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserById(Long id);
}
