package com.example.backend.user.mapper;

import com.example.backend.Security.entity.UserInfo;
import com.example.backend.user.dto.UserRequest;
import com.example.backend.user.dto.UserResponse;

public class UserMapper {

    public static UserResponse toResponse(UserInfo userInfo) {
        if (userInfo == null) {
            return null;
        }
        UserResponse response = new UserResponse();
        response.setId(userInfo.getId());
        response.setUsername(userInfo.getUserName());
        response.setRole(userInfo.getRole());
        return response;
    }

    public static UserInfo toEntity(UserRequest request) {
        if (request == null) {
            return null;
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserName(request.getUsername());
        userInfo.setPassword(request.getPassword());
        userInfo.setRole(request.getRole());
        return userInfo;
    }
}
