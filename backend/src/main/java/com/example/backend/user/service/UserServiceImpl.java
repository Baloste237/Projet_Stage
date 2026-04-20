package com.example.backend.user.service;

import com.example.backend.Security.entity.UserInfo;
import com.example.backend.scan.repository.UserInfoRepository;
import com.example.backend.user.dto.UserRequest;
import com.example.backend.user.dto.UserResponse;
import com.example.backend.user.exception.UserNotFoundException;
import com.example.backend.user.exception.UsernameAlreadyExistsException;
import com.example.backend.user.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserInfoRepository userInfoRepository, PasswordEncoder passwordEncoder) {
        this.userInfoRepository = userInfoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse createUser(UserRequest request) {
        if (userInfoRepository.existsByUserName(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        UserInfo userInfo = UserMapper.toEntity(request);
        userInfo.setPassword(passwordEncoder.encode(request.getPassword()));
        
        UserInfo savedUser = userInfoRepository.save(userInfo);
        return UserMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest request) {
        UserInfo existingUser = userInfoRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (!existingUser.getUserName().equals(request.getUsername()) && 
            userInfoRepository.existsByUserName(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        existingUser.setUserName(request.getUsername());
        // Only update password if it's provided in the request
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        existingUser.setRole(request.getRole());

        UserInfo updatedUser = userInfoRepository.save(existingUser);
        return UserMapper.toResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userInfoRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userInfoRepository.deleteById(id);
    }

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userInfoRepository.findAll(pageable)
                .map(UserMapper::toResponse);
    }

    @Override
    public UserResponse getUserById(Long id) {
        UserInfo userInfo = userInfoRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return UserMapper.toResponse(userInfo);
    }
}
