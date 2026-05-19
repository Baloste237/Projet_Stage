package com.example.backend.Security.mapper;

import com.example.backend.Security.dto.UserInfoDto;
import com.example.backend.Security.entity.UserInfo;

import com.example.backend.user.domain.Role;

public class UserInfoMapper {
    public static UserInfoDto toDto(UserInfo userInfo){
        UserInfoDto dto = new UserInfoDto();
        dto.setUserName(userInfo.getUserName());
        dto.setEmail(userInfo.getEmail());
        dto.setPassword(userInfo.getPassword());
        dto.setRole(userInfo.getRole() != null ? userInfo.getRole().name() : null);
        return dto;
    }

    public static UserInfo toEntity(UserInfoDto userInfoDto){
        return new UserInfo(
            userInfoDto.getUserName(),
            userInfoDto.getEmail(),
            userInfoDto.getPassword(),
            userInfoDto.getRole() != null ? Role.valueOf(userInfoDto.getRole()) : Role.ROLE_ANALYSTE_SECURITE
        );
    }
}
