package com.example.backend.Security.service.implementation;

import com.example.backend.Security.mapper.UserInfoMapper;
import com.example.backend.Security.dto.UserInfoDto;
import com.example.backend.Security.entity.UserInfo;
import com.example.backend.Security.service.JWTService;
import com.example.backend.scan.repository.UserInfoRepository;
import com.example.backend.Security.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public JWTService jwtService;


    @Override
    public UserInfoDto createUser(UserInfoDto userInfoDto) {
        UserInfo userInfo= UserInfoMapper.toEntity(userInfoDto);
        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        userInfoRepository.save(userInfo);

        return UserInfoMapper.toDto(userInfo);
    }

    @Override
    public String getUserInfo(UserInfoDto userInfoDto) {
        Authentication authentication= authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userInfoDto.getUserName(), userInfoDto.getPassword()));
        if (authentication.isAuthenticated())
            return jwtService.generateToken(authentication.getName());
        return "Failure";
    }

}
