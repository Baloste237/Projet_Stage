package com.example.backend.user.dto;

import com.example.backend.user.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse contenant les informations d'un utilisateur (sans mot de passe)")
public class UserResponse {
    @Schema(description = "ID unique de l'utilisateur", example = "1")
    private Long id;

    @Schema(description = "Nom d'utilisateur", example = "analyste_dupont")
    private String username;

    @Schema(description = "Rôle de l'utilisateur", example = "ROLE_ANALYSTE")
    private Role role;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
