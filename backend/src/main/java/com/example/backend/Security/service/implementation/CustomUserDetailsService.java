//package com.example.backend.Security.service.implementation;
//
//import com.example.backend.Security.entity.UserInfo;
//import com.example.backend.scan.repository.UserInfoRepository;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.authority.AuthorityUtils;
//import org.springframework.security.core.userdetails.*;
//import org.springframework.stereotype.Service;
//
//@Service
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
//
//    @Autowired
//    private UserInfoRepository repository;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        logger.info("Loading user: {}", username);
//
//        try {
//            UserInfo user = repository.findByUserName(username)
//                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
//
//            logger.info("User found: {} with roles: {}", username, user.getRoles());
//
//            return User.builder()
//                    .username(user.getUserName())
//                    .password(user.getPassword())
//                    .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRoles()))
//                    .build();
//        } catch (Exception e) {
//            logger.error("Error loading user: {}", username, e);
//            throw new UsernameNotFoundException("User not found: " + username, e);
//        }
//    }
//}