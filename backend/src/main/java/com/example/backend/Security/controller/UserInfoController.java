package com.example.backend.Security.controller;

import com.example.backend.Security.dto.UserInfoDto;
import com.example.backend.Security.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
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

    @GetMapping("oauth2/user")
    public ResponseEntity<String> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            String email = principal.getAttribute("email");
            String token = userInfoService.generateTokenForOAuth2User(email);
            return new ResponseEntity<>(token, HttpStatus.OK);
        }
        return new ResponseEntity<>("No authenticated user", HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("oauth2/google")
    public ResponseEntity<String> googleLogin() {
        return new ResponseEntity<>("Redirect to /oauth2/authorization/google", HttpStatus.OK);
    }

    @GetMapping("oauth2/github")
    public ResponseEntity<String> githubLogin() {
        return new ResponseEntity<>("Redirect to /oauth2/authorization/github", HttpStatus.OK);
    }

    @GetMapping("oauth2/error")
    public ResponseEntity<String> handleOAuth2Error(@RequestParam String error,
                                                   @RequestParam(required = false) String provider) {
        String message = String.format("OAuth2 authentication failed for provider %s: %s",
                                     provider != null ? provider : "unknown", error);
        return new ResponseEntity<>(message, HttpStatus.UNAUTHORIZED);
    }

}
