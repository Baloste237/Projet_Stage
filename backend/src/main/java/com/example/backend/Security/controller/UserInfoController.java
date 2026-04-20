package com.example.backend.Security.controller;

import com.example.backend.Security.dto.UserInfoDto;
import com.example.backend.Security.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserInfoController {

    @Autowired
    UserInfoService userInfoService;

    @PostMapping("register")
    public ResponseEntity<String> createUserInfo(@RequestBody UserInfoDto userInfoDto){
        UserInfoDto userInfoDto1=userInfoService.createUser(userInfoDto);
        return new ResponseEntity<>("user" + userInfoDto1.getUserName()+"is created", HttpStatus.CREATED);
    }

    @PostMapping("login")
    public ResponseEntity<String> getUSerInfo(@RequestBody UserInfoDto userInfoDto){
        return new ResponseEntity<>(userInfoService.getUserInfo(userInfoDto),HttpStatus.OK);
    }



}
