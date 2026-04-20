package com.example.backend.Security.service;

import com.example.backend.Security.dto.UserInfoDto;

public interface UserInfoService {
    public UserInfoDto createUser(UserInfoDto userInfoDto);

    public String getUserInfo(UserInfoDto userInfoDto);

}
