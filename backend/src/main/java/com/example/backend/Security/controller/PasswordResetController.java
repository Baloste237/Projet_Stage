package com.example.backend.Security.controller;

import com.example.backend.Security.dto.ForgotPasswordRequest;
import com.example.backend.Security.dto.ResetPasswordRequest;
import com.example.backend.Security.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "Gestion de la réinitialisation de mot de passe")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private Environment env;

    @Operation(
            summary = "Demande de réinitialisation de mot de passe",
            description = """
                    Envoie un e-mail contenant un lien de réinitialisation de mot de passe.
                    En mode **dev**, le token est également retourné dans la réponse JSON pour faciliter les tests.
                    Le lien expire après **1 heure**.
                    
                    > ⚠️ Pour des raisons de sécurité, la réponse est toujours positive même si l'email n'existe pas.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Demande traitée avec succès",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Si un compte est associé à cet email, un lien de réinitialisation vous a été envoyé.",
                                              "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
                                            }
                                            """))
            ),
            @ApiResponse(responseCode = "400", description = "Email invalide ou champ manquant",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur lors de l'envoi de l'e-mail",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String token = passwordResetService.processForgotPassword(request.getEmail());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Si un compte est associé à cet email, un lien de réinitialisation vous a été envoyé.");
        
        // Return token in response only for dev profile
        if (token != null && Arrays.asList(env.getActiveProfiles()).contains("dev")) {
            response.put("token", token);
        }
        
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Réinitialisation du mot de passe",
            description = """
                    Réinitialise le mot de passe d'un utilisateur à l'aide d'un token valide.
                    Le nouveau mot de passe doit respecter les contraintes de sécurité :
                    - Minimum **8 caractères**
                    - Au moins **1 chiffre**, **1 majuscule**, **1 minuscule**, **1 caractère spécial**
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Mot de passe réinitialisé avec succès",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Mot de passe réinitialisé avec succès.\"}"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Token invalide, expiré ou mot de passe non conforme",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Token invalide ou expiré.\"}"))
            )
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            passwordResetService.processResetPassword(request.getToken(), request.getNewPassword());
            response.put("message", "Mot de passe réinitialisé avec succès.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
