package com.example.backend.user.dto;

import com.example.backend.user.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requête de création ou modification d'un utilisateur par un administrateur")
public class UserRequest {
    @Schema(description = "Nom d'utilisateur", example = "analyste_dupont", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "Mot de passe de l'utilisateur", example = "S3cur3P@ss!", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    @Schema(description = "Rôle assigné à l'utilisateur", example = "ROLE_ANALYSTE")
    private Role role;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
