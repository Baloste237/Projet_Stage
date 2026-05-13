package com.example.backend.Security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Requête de demande de réinitialisation de mot de passe")
public class ForgotPasswordRequest {
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Schema(description = "Adresse e-mail associée au compte", example = "utilisateur@vulnscan.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
}
