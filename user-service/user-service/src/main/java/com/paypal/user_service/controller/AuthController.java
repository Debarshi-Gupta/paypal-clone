package com.paypal.user_service.controller;

import com.paypal.user_service.model.dto.JwtResponse;
import com.paypal.user_service.model.dto.LoginRequest;
import com.paypal.user_service.model.dto.SignupRequest;
import com.paypal.user_service.model.dto.UserResponse;
import com.paypal.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        log.info("Signup API called");
        return ResponseEntity.ok(userService.registerUser(signupRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login API called");
        return ResponseEntity.ok(userService.loginUser(loginRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("Get user by id API called");
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
