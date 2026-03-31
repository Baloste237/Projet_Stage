package com.example.backend.Security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "users")
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userName;
    private String password;
    private String roles;
    private String email;
    private String provider; // GOOGLE, GITHUB, GITLAB, LOCAL
    private String providerId; // OAuth2 provider ID

    public UserInfo(String userName, String password, String roles) {
        this.userName = userName;
        this.password = password;
        this.roles = roles;
        this.provider = "LOCAL";
    }

    public UserInfo() {

    }

    public UserInfo(String userName, String email, String provider, String providerId, String roles) {
        this.userName = userName;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
        this.roles = roles;
        this.password = ""; // No password for OAuth2 users
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
