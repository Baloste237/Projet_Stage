package com.example.backend.Security.service;

import com.example.backend.Security.entity.UserInfo;
import com.example.backend.user.domain.Role;
import java.util.List;

public interface AdminService {
    List<UserInfo> getAllUsers();
    UserInfo toggleUserStatus(Long userId);
    UserInfo changeUserRole(Long userId, Role newRole);
}
