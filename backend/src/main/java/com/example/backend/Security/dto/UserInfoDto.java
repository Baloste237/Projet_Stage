package com.example.backend.Security.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO pour l'inscription et la connexion d'un utilisateur")
public class UserInfoDto {
    @Schema(description = "Nom d'utilisateur unique", example = "admin_user", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userName;

    @Schema(description = "Adresse e-mail de l'utilisateur", example = "admin@vulnscan.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Mot de passe (hashé en BCrypt côté serveur)", example = "P@ssw0rd!2024", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    @Schema(description = "Rôle de l'utilisateur", example = "ROLE_ANALYSTE", allowableValues = {"ROLE_ADMIN", "ROLE_ANALYSTE"})
    private String role;

    public UserInfoDto(String userName, String email, String password, String role) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public UserInfoDto() {}

    // Getters and setters
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
