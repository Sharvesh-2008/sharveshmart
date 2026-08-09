package com.digitalmarketplace.controller;

import com.digitalmarketplace.dto.RegisterRequest;
import com.digitalmarketplace.dto.UserResponse;
import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.entity.UserRole;
import com.digitalmarketplace.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Public registration endpoint")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Register a new user account")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserRole role = request.role() == null ? UserRole.USER : UserRole.valueOf(request.role());
        User user = userService.createUser(request.name(), request.email(), request.password(), role);
        UserResponse response = UserResponse.from(user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, "/api/users/" + user.getId())
                .body(response);
    }
}
