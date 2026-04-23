package com.paypal.user_service.service;

import com.paypal.user_service.exception.InvalidCredentialsException;
import com.paypal.user_service.exception.UserAlreadyExistsException;
import com.paypal.user_service.exception.UserNotFoundException;
import com.paypal.user_service.model.dto.JwtResponse;
import com.paypal.user_service.model.dto.LoginRequest;
import com.paypal.user_service.model.dto.SignupRequest;
import com.paypal.user_service.model.dto.UserResponse;
import com.paypal.user_service.model.entity.Role;
import com.paypal.user_service.model.entity.User;
import com.paypal.user_service.repository.UserRepository;
import com.paypal.user_service.service.feign.WalletClient;
import com.paypal.user_service.service.mapper.UserMapper;
import com.paypal.user_service.service.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final WalletClient walletClient;

    @Value("${admin.secret.key}")
    private String adminSecretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Override
    public UserResponse registerUser(SignupRequest signupRequest) {

        log.info("Registering user with email: {}", signupRequest.getEmail());

        userRepository.findByEmail(signupRequest.getEmail())
                .ifPresent(user -> {
                    log.warn("User already exists with email: {}", signupRequest.getEmail());
                    throw new UserAlreadyExistsException("User already exists");
                });

        User user = userMapper.toEntity(signupRequest);

        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        if (signupRequest.getAdminKey() != null &&
                signupRequest.getAdminKey().equals(adminSecretKey)) {
            user.setRole(Role.ROLE_ADMIN);
        } else {
            user.setRole(Role.ROLE_USER);
        }

        User savedUser = userRepository.save(user);

        try {
            walletClient.createWallet(savedUser.getId());
        } catch (Exception ex) {
            log.error("Failed to create wallet for user {}", savedUser.getId(), ex);
            userRepository.delete(savedUser);
            throw new RuntimeException(ex.getMessage());
        }

        log.info("User registered successfully with id: {}", savedUser.getId());

        return userMapper.toResponse(savedUser);
    }

    @Override
    public JwtResponse loginUser(LoginRequest loginRequest) {

        log.info("Login attempt for email: {}", loginRequest.getEmail());

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", loginRequest.getEmail());
                    return new InvalidCredentialsException("Invalid email or password");
                });

        if (!user.isActive() || !user.isAccountNonLocked()) {
            log.warn("Inactive or locked user: {}", loginRequest.getEmail());
            throw new InvalidCredentialsException("Account is inactive or locked");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Invalid password for email: {}", loginRequest.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtils.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        log.info("User logged in successfully: {}", user.getEmail());

        return JwtResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresIn(jwtExpiration)
                .build();
    }

    @Override
    public UserResponse getUserById(Long id) {

        log.info("Fetching user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new UserNotFoundException("User id not found");
                });

        return userMapper.toResponse(user);
    }
}
