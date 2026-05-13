package com.example.backend.Security.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import com.example.backend.user.domain.Role;


@Entity
@Table(name = "users")
@Schema(description = "Entité utilisateur du système VulnScan Pro")
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID unique de l'utilisateur", example = "1")
    private Long id;

    @Column(unique = true)
    @Schema(description = "Nom d'utilisateur unique", example = "admin_user")
    private String userName;

    @Column(unique = true)
    @Schema(description = "Adresse e-mail unique", example = "admin@vulnscan.com")
    private String email;

    @Schema(description = "Mot de passe hashé (BCrypt)", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Rôle de l'utilisateur", example = "ROLE_ADMIN")
    private Role role;
    
    @Column(nullable = false, columnDefinition = "boolean default true")
    @Schema(description = "Indique si le compte est actif", example = "true")
    private boolean enabled = true;

    public UserInfo(String userName, String email, String password, Role role) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = true;
    }

    public UserInfo() {

    }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
