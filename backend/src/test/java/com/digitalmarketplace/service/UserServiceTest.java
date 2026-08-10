package com.digitalmarketplace.service;

import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.entity.UserRole;
import com.digitalmarketplace.exception.BusinessException;
import com.digitalmarketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void createUserDefaultsToBuyerRoleAndHashesPassword() {
        when(passwordEncoder.encode("secret")).thenReturn("$2a$10$hashed");
        when(userRepository.save(any(User.class))).then(returnsFirstArg());

        User result = userService.createUser("Alice", "alice@example.com", "secret", null);

        assertEquals(UserRole.USER, result.getRole());
        assertEquals("$2a$10$hashed", result.getPasswordHash());
        assertNotEquals("secret", result.getPasswordHash());
        verify(passwordEncoder).encode("secret");
    }

    @Test
    void createUserHonoursExplicitRole() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).then(returnsFirstArg());

        User result = userService.createUser("Bob", "bob@example.com", "pw", UserRole.SELLER);

        assertEquals(UserRole.SELLER, result.getRole());
    }

    @Test
    void createUserRejectsDuplicateEmail() {
        when(userRepository.findByEmail("dup@example.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(BusinessException.class,
                () -> userService.createUser("Dup", "dup@example.com", "pw", null));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserRejectsBlankEmail() {
        assertThrows(BusinessException.class,
                () -> userService.createUser("X", "   ", "pw", null));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByIdDelegatesToRepository() {
        User user = new User();
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        assertEquals(user, userService.findById(7L).orElseThrow(AssertionError::new));
    }

    @Test
    void findByEmailDelegatesToRepository() {
        User user = new User();
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));

        assertEquals(user, userService.findByEmail("a@b.com").orElseThrow(AssertionError::new));
    }

    @Test
    void authenticateReturnsUserWhenCredentialsMatch() {
        User user = new User();
        user.setId(5L);
        user.setEmail("alice@example.com");
        user.setPasswordHash("$2a$10$hashed");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "$2a$10$hashed")).thenReturn(true);

        assertEquals(user, userService.authenticate("alice@example.com", "secret"));
    }

    @Test
    void authenticateThrowsWhenUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> userService.authenticate("missing@example.com", "secret"));
    }

    @Test
    void authenticateThrowsWhenPasswordDoesNotMatch() {
        User user = new User();
        user.setPasswordHash("$2a$10$hashed");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "$2a$10$hashed")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> userService.authenticate("alice@example.com", "wrong"));
    }
}