package com.example.backend.scan.repository;

import com.example.backend.Security.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {

    Optional<UserInfo> findByUserName(String userName);
    Optional<UserInfo> findByUserNameOrEmail(String userName, String email);

    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);
}

