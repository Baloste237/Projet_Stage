package com.example.backend.scan.exception;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestionnaire global des exceptions — fournit des réponses d'erreur uniformes et documentées.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ScanException.class)
    @ApiResponse(responseCode = "400", description = "Erreur liée au scan (fichier invalide, type non supporté, moteur indisponible)",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"error\": \"Scan Error\", \"message\": \"Type d'application non supporté\", \"timestamp\": \"2026-05-12T10:30:00\"}")))
    public ResponseEntity<Map<String, String>> handleScanException(ScanException ex) {
        log.error("Scan exception: {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Scan Error");
        error.put("message", ex.getMessage());
        error.put("timestamp", LocalDateTime.now().toString());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ApiResponse(responseCode = "400", description = "Taille du fichier uploadé dépassée (max 100 MB)",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"error\": \"File Size Exceeded\", \"message\": \"The uploaded file is too large. Maximum allowed size is 100MB.\"}")))
    public ResponseEntity<Map<String, String>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.error("File size exceeded: {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "File Size Exceeded");
        error.put("message", "The uploaded file is too large. Maximum allowed size is 100MB.");
        error.put("timestamp", LocalDateTime.now().toString());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ApiResponse(responseCode = "400", description = "Argument invalide dans la requête",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"error\": \"Bad Request\", \"message\": \"Paramètre invalide\"}")))
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument error: {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());
        error.put("timestamp", LocalDateTime.now().toString());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ApiResponse(responseCode = "400", description = "Erreur de validation des champs (contraintes @NotBlank, @Email, @Pattern, etc.)",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"error\": \"Validation Error\", \"message\": \"email: Format d'email invalide; password: Le mot de passe est obligatoire\"}")))
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        Map<String, String> error = new HashMap<>();
        error.put("error", "Validation Error");
        error.put("message", details);
        error.put("timestamp", LocalDateTime.now().toString());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @ApiResponse(responseCode = "403", description = "Accès refusé — rôle insuffisant",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"error\": \"Access Denied\", \"message\": \"Vous n'avez pas les permissions nécessaires pour cette action.\"}")))
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Access Denied");
        error.put("message", "Vous n'avez pas les permissions nécessaires pour cette action.");
        error.put("timestamp", LocalDateTime.now().toString());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(org.springframework.mail.MailException.class)
    @ApiResponse(responseCode = "500", description = "Échec de l'envoi d'e-mail",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"error\": \"Email Sending Failed\", \"message\": \"Le serveur n'a pas pu envoyer l'e-mail.\"}")))
    public ResponseEntity<Map<String, String>> handleMailException(org.springframework.mail.MailException ex) {
        log.error("Mail exception: {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Email Sending Failed");
        error.put("message", "Le serveur n'a pas pu envoyer l'e-mail. Veuillez vérifier la configuration de votre serveur de messagerie.");
        error.put("timestamp", LocalDateTime.now().toString());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    @ApiResponse(responseCode = "500", description = "Erreur interne inattendue",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"error\": \"Internal Server Error\", \"message\": \"An unexpected error occurred.\"}")))
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal Server Error");
        error.put("message", "An unexpected error occurred.");
        error.put("timestamp", LocalDateTime.now().toString());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}