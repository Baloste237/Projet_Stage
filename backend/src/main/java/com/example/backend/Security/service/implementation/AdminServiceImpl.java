package com.example.backend.Security.service.implementation;

import com.example.backend.Security.entity.UserInfo;
import com.example.backend.scan.repository.UserInfoRepository;
import com.example.backend.Security.service.AdminService;
import com.example.backend.user.domain.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserInfoRepository userInfoRepository;

    public AdminServiceImpl(UserInfoRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }

    @Override
    public List<UserInfo> getAllUsers() {
        return userInfoRepository.findAll();
    }

    @Override
    @Transactional
    public UserInfo toggleUserStatus(Long userId) {
        UserInfo user = userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Validation : prevent an admin from disabling themselves if they are the only admin.
        // Or we can just trust the caller for now, but let's add a basic check.
        if (user.getRole() == Role.ROLE_ADMIN && user.isEnabled()) {
            long adminCount = userInfoRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ROLE_ADMIN && u.isEnabled())
                    .count();
            if (adminCount <= 1) {
                throw new RuntimeException("Cannot disable the last active admin");
            }
        }

        user.setEnabled(!user.isEnabled());
        return userInfoRepository.save(user);
    }

    @Override
    @Transactional
    public UserInfo changeUserRole(Long userId, Role newRole) {
        UserInfo user = userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.ROLE_ADMIN && newRole != Role.ROLE_ADMIN) {
            long adminCount = userInfoRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ROLE_ADMIN && u.isEnabled())
                    .count();
            if (adminCount <= 1) {
                throw new RuntimeException("Cannot remove the last admin");
            }
        }

        user.setRole(newRole);
        return userInfoRepository.save(user);
    }
}
