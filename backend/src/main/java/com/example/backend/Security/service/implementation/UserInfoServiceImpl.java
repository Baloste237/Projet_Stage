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
        if (userInfoRepository.existsByEmail(userInfoDto.getEmail())) {
            throw new IllegalArgumentException("L'adresse e-mail est déjà utilisée.");
        }
        if (userInfoRepository.existsByUserName(userInfoDto.getUserName())) {
            throw new IllegalArgumentException("Le nom d'utilisateur est déjà pris.");
        }

        UserInfo userInfo= UserInfoMapper.toEntity(userInfoDto);
        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        userInfoRepository.save(userInfo);

        return UserInfoMapper.toDto(userInfo);
    }

    @Override
    public String getUserInfo(UserInfoDto userInfoDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userInfoDto.getUserName(), userInfoDto.getPassword()));
        if (authentication.isAuthenticated()) {
            // Inclure le rôle dans le token JWT pour que le frontend puisse le lire
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority())
                    .orElse("ROLE_ANALYSTE");
            java.util.Map<String, Object> claims = new java.util.HashMap<>();
            claims.put("role", role);
            return jwtService.generateToken(authentication.getName(), claims);
        }
        return "Failure";
    }

}
