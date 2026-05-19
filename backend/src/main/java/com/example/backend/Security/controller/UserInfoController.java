package com.example.backend.Security.controller;

import com.example.backend.Security.dto.UserInfoDto;
import com.example.backend.Security.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.security.Principal;
import java.util.Map;
import java.util.HashMap;
import com.example.backend.scan.repository.UserInfoRepository;
import com.example.backend.Security.entity.UserInfo;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "Inscription et connexion des utilisateurs avec génération de token JWT")
public class UserInfoController {

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    UserInfoRepository userInfoRepository;


    @Operation(
            summary = "Inscription d'un nouvel utilisateur",
            description = """
                    Crée un nouveau compte utilisateur dans le système.
                    Le mot de passe est automatiquement hashé avec BCrypt.
                    Le rôle par défaut est ROLE_ANALYSTE_SECURITE sauf si spécifié autrement.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Utilisateur créé avec succès",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "\"user admin is created\""))
            ),
            @ApiResponse(responseCode = "400", description = "Données invalides ou utilisateur déjà existant",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("register")
    public ResponseEntity<String> createUserInfo(@RequestBody UserInfoDto userInfoDto){
        UserInfoDto userInfoDto1=userInfoService.createUser(userInfoDto);
        return new ResponseEntity<>("user" + userInfoDto1.getUserName()+"is created", HttpStatus.CREATED);
    }

    @Operation(
            summary = "Connexion utilisateur",
            description = """
                    Authentifie un utilisateur et retourne un token JWT valide.
                    Le token doit être utilisé dans le header `Authorization: Bearer <token>`
                    pour accéder aux endpoints protégés.
                    **Durée de validité** : 24 heures.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Connexion réussie — Token JWT retourné",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\""))
            ),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides — Email ou mot de passe incorrect",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("login")
    public ResponseEntity<String> getUSerInfo(@RequestBody UserInfoDto userInfoDto){
        return new ResponseEntity<>(userInfoService.getUserInfo(userInfoDto),HttpStatus.OK);
    }

    @Operation(summary = "Récupérer l'utilisateur connecté", description = "Retourne les informations de l'utilisateur basé sur son JWT.")
    @GetMapping("me")
    public ResponseEntity<?> getMe(Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return userInfoRepository.findByUserName(principal.getName())
            .map(user -> {
                Map<String, Object> resp = new HashMap<>();
                resp.put("id", user.getId());
                resp.put("username", user.getUserName());
                resp.put("email", user.getEmail());
                resp.put("role", user.getRole().name());
                // Add dummy dates or real if they exist in UserInfo
                return ResponseEntity.ok(resp);
            })
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }



}
