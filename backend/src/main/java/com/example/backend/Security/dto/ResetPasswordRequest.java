package com.example.backend.Security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Requête de réinitialisation du mot de passe avec token de validation")
public class ResetPasswordRequest {
    @NotBlank(message = "Le token est obligatoire")
    @Schema(description = "Token de réinitialisation reçu par e-mail", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$", 
             message = "Le mot de passe doit contenir au moins 8 caractères, un chiffre, une majuscule, une minuscule et un caractère spécial.")
    @Schema(description = "Nouveau mot de passe (min 8 car., 1 chiffre, 1 maj., 1 min., 1 spécial)", example = "N3wP@ssw0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;
}
