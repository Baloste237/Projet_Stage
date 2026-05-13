package com.example.backend.Security.service.implementation;

import com.example.backend.Security.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Réinitialisation de votre mot de passe");
        message.setText("Pour réinitialiser votre mot de passe, veuillez cliquer sur le lien suivant :\n\n"
                + "http://localhost:3000/reset-password?token=" + token
                + "\n\nCe lien expirera dans 15 minutes.");
        emailSender.send(message);
    }
}
