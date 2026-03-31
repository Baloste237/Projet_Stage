package com.example.backend.Security.mapper;

import com.example.backend.Security.dto.UserInfoDto;
import com.example.backend.Security.entity.UserInfo;

public class UserInfoMapper {
    public static UserInfoDto toDto(UserInfo userInfo){
        UserInfoDto dto = new UserInfoDto();
        dto.setUserName(userInfo.getUserName());
        dto.setPassword(userInfo.getPassword());
        dto.setRoles(userInfo.getRoles());
        dto.setEmail(userInfo.getEmail());
        dto.setProvider(userInfo.getProvider());
        dto.setProviderId(userInfo.getProviderId());
        return dto;
    }

    public static UserInfo toEntity(UserInfoDto userInfoDto){
        if (userInfoDto.getProvider() != null && !userInfoDto.getProvider().equals("LOCAL")) {
            return new UserInfo(
                userInfoDto.getUserName(),
                userInfoDto.getEmail(),
                userInfoDto.getProvider(),
                userInfoDto.getProviderId(),
                userInfoDto.getRoles()
            );
        } else {
            return new UserInfo(
                userInfoDto.getUserName(),
                userInfoDto.getPassword(),
                userInfoDto.getRoles()
            );
        }
    }
}
