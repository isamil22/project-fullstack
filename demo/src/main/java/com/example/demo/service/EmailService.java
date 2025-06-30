package com.example.demo.service;

import com.example.demo.model.Order;
import com.example.demo.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOrderConfirmation(Order order){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(order.getUser().getEmail());
        message.setSubject("Order confirmation");
        message.setText("Your order has been confirmed. Order ID " + order.getId());
        mailSender.send(message);
    }

    // --- THIS METHOD IS UPDATED ---
    public void sendConfirmationCode(User user){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("Confirm your email for BeautyCosmetics");

        // This creates a full link for the user to click, which is better practice.
        // It assumes your frontend has a page at /confirm-email that can handle the code.
        String confirmationUrl = "http://localhost:8081/confirm-email?code=" + user.getConfirmationCode();

        message.setText("Thank you for registering! Please click the link below to activate your account:\n" + confirmationUrl + "\n\nAlternatively, you can enter this code on the confirmation page: " + user.getConfirmationCode());

        mailSender.send(message);
    }

    public void sendPasswordResetEmail(User user, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, please click the link below:\n" + resetLink);
        mailSender.send(message);
    }
}