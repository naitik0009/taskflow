package com.taskflow.controller;

import com.taskflow.dto.AuthDtos.AuthResponse;
import com.taskflow.dto.AuthDtos.LoginRequest;
import com.taskflow.dto.AuthDtos.RegisterRequest;
import com.taskflow.dto.UserDto;
import com.taskflow.security.CurrentUser;
import com.taskflow.security.UserPrincipal;
import com.taskflow.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user and return a JWT")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and return a JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Return the currently authenticated user")
    public UserDto me() {
        UserPrincipal principal = CurrentUser.principal();
        return new UserDto(principal.getId(), principal.getUsername(), principal.getDisplayName());
    }
}
