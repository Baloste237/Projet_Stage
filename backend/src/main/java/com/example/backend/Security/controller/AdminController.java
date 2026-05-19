package com.example.backend.Security.controller;

import com.example.backend.Security.entity.UserInfo;
import com.example.backend.Security.service.AdminService;
import com.example.backend.user.domain.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Administration", description = "Gestion avancée des comptes utilisateurs — Réservé ROLE_ADMIN")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(
            summary = "Lister tous les utilisateurs",
            description = "Récupère la liste complète des utilisateurs enregistrés dans le système. Réservé aux administrateurs."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des utilisateurs récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié — Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé — Rôle ADMIN requis")
    })
    @GetMapping("/users")
    public ResponseEntity<List<UserInfo>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @Operation(
            summary = "Activer / Désactiver un utilisateur",
            description = "Bascule le statut d'activation d'un compte utilisateur (enabled ↔ disabled). Un utilisateur désactivé ne peut plus se connecter."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statut de l'utilisateur modifié avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié — Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé — Rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @PatchMapping("/users/{id}/toggle-status")
    public ResponseEntity<UserInfo> toggleUserStatus(
            @Parameter(description = "ID de l'utilisateur", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleUserStatus(id));
    }

    @Operation(
            summary = "Changer le rôle d'un utilisateur",
            description = """
                    Modifie le rôle d'un utilisateur existant.
                    Rôles disponibles : `ROLE_ADMIN`, `ROLE_ANALYSTE_SECURITE`.
                    Le préfixe `ROLE_` est ajouté automatiquement s'il est absent.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rôle modifié avec succès"),
            @ApiResponse(responseCode = "400", description = "Rôle invalide ou champ 'role' manquant",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Non authentifié — Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé — Rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserInfo> changeUserRole(
            @Parameter(description = "ID de l'utilisateur", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objet contenant le nouveau rôle",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"role\": \"ADMIN\"}")))
            @RequestBody Map<String, String> request) {
        String roleStr = request.get("role");
        if (roleStr == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!roleStr.startsWith("ROLE_")) {
            roleStr = "ROLE_" + roleStr;
        }
        Role newRole = Role.valueOf(roleStr.toUpperCase());
        return ResponseEntity.ok(adminService.changeUserRole(id, newRole));
    }
}
