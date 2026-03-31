package com.example.backend.Security.service.implementation;

import com.example.backend.Security.mapper.UserInfoUserDetailsMapper;
import com.example.backend.Security.entity.UserInfo;
import com.example.backend.scan.repository.UserInfoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserInfoUserDetailsServiceImpl implements UserDetailsService {


    private final UserInfoRepository userInfoRepository;

    // ✅ Injection par constructeur (RECOMMANDÉ)
    public UserInfoUserDetailsServiceImpl(UserInfoRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserInfo> userInfo=userInfoRepository.findByUserName(username);
         return userInfo.map(UserInfoUserDetailsMapper::new)
                 .orElseThrow(() -> new UsernameNotFoundException("User" + username + "Not Found" ));
    }
}
