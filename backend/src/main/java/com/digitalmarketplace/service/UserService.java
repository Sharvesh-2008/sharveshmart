package com.digitalmarketplace.service;

import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.entity.UserRole;
import com.digitalmarketplace.exception.BusinessException;
import com.digitalmarketplace.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User createUser(String name, String email, String rawPassword, UserRole role) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("Email is required");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("An account with that email already exists");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role == null ? UserRole.USER : role);
        return userRepository.save(user);
    }
}