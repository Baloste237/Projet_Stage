package com.example.backend.Security.service.implementation;

import com.example.backend.Security.entity.PasswordResetToken;
import com.example.backend.Security.entity.UserInfo;
import com.example.backend.Security.service.EmailService;
import com.example.backend.Security.service.PasswordResetService;
import com.example.backend.scan.repository.PasswordResetTokenRepository;
import com.example.backend.scan.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public String processForgotPassword(String email) {
        // Do not throw exception if email not found to prevent username enumeration
        Optional<UserInfo> optionalUser = userInfoRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            UserInfo user = optionalUser.get();
            
            // Generate token
            String token = UUID.randomUUID().toString();
            
            // Delete old token if exists
            tokenRepository.deleteByEmail(user.getEmail());
            
            // Save new token, valid for 15 minutes
            PasswordResetToken resetToken = new PasswordResetToken(token, user.getEmail(), LocalDateTime.now().plusMinutes(15));
            tokenRepository.save(resetToken);
            
            // Send email
            emailService.sendPasswordResetEmail(user.getEmail(), token);
            return token;
        }
        return null;
    }

    @Override
    @Transactional
    public void processResetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> optionalToken = tokenRepository.findByToken(token);
        
        if (optionalToken.isEmpty()) {
            throw new RuntimeException("Token invalide.");
        }
        
        PasswordResetToken resetToken = optionalToken.get();
        if (resetToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            tokenRepository.deleteByToken(token);
            throw new RuntimeException("Token expiré.");
        }
        
        Optional<UserInfo> optionalUser = userInfoRepository.findByEmail(resetToken.getEmail());
        if (optionalUser.isPresent()) {
            UserInfo user = optionalUser.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userInfoRepository.save(user);
        }
        
        tokenRepository.deleteByToken(token);
    }
}
