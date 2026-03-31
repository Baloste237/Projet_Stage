package com.example.backend.Security.dto;

public class UserInfoDto {
    private String userName;
    private String password;
    private String roles;
    private String email;
    private String provider;
    private String providerId;

    // Constructor for local registration
    public UserInfoDto(String userName, String password, String roles) {
        this.userName = userName;
        this.password = password;
        this.roles = roles;
        this.provider = "LOCAL";
    }

    // Constructor for OAuth2
    public UserInfoDto(String userName, String email, String provider, String providerId, String roles) {
        this.userName = userName;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
        this.roles = roles;
    }

    public UserInfoDto() {}

    // Getters and setters
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
}
