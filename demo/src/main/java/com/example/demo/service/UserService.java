package com.example.demo.service;

import com.example.demo.dto.SignupRequest;
import com.example.demo.model.ERole;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repositories.RoleRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public User registerUser(SignupRequest signupRequest) {
        User user = new User(signupRequest.getUsername(),
                signupRequest.getEmail(),
                passwordEncoder.encode(signupRequest.getPassword()));

        user.setEmailConfirmed(false);
        user.setConfirmationCode(generateConfirmationCode());

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        emailService.sendConfirmationEmail(savedUser.getEmail(), savedUser.getConfirmationCode());
        return savedUser;
    }

    public void confirmEmail(String confirmationCode) {
        User user = userRepository.findByConfirmationCode(confirmationCode)
                .orElseThrow(() -> new RuntimeException("Invalid confirmation code."));

        user.setEmailConfirmed(true);
        user.setConfirmationCode(null);
        userRepository.save(user);
    }

    public void resendConfirmationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email."));

        if (user.isEmailConfirmed()) {
            throw new IllegalStateException("Email is already confirmed.");
        }

        user.setConfirmationCode(generateConfirmationCode());
        userRepository.save(user);
        emailService.sendConfirmationEmail(user.getEmail(), user.getConfirmationCode());
    }

    private String generateConfirmationCode() {
        return UUID.randomUUID().toString();
    }
}