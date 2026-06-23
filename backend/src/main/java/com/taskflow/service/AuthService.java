package com.taskflow.service;

import com.taskflow.dto.AuthDtos.AuthResponse;
import com.taskflow.dto.AuthDtos.LoginRequest;
import com.taskflow.dto.AuthDtos.RegisterRequest;
import com.taskflow.entity.User;
import com.taskflow.exception.BadRequestException;
import com.taskflow.mapper.DtoMapper;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final DtoMapper mapper;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       DtoMapper mapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mapper = mapper;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists");
        }
        User user = User.builder()
                .email(email)
                .displayName(request.displayName().trim())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();
        user = userRepository.save(user);
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, mapper.toUserDto(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().toLowerCase().trim();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, mapper.toUserDto(user));
    }
}
