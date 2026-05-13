package com.example.backend.Security.service;

public interface PasswordResetService {
    String processForgotPassword(String email);
    void processResetPassword(String token, String newPassword);
}
