package com.paypal.user_service.service;

import com.paypal.user_service.model.dto.JwtResponse;
import com.paypal.user_service.model.dto.LoginRequest;
import com.paypal.user_service.model.dto.SignupRequest;
import com.paypal.user_service.model.dto.UserResponse;

public interface UserService {

    UserResponse registerUser(SignupRequest signupRequest);

    JwtResponse loginUser(LoginRequest loginRequest);

    UserResponse getUserById(Long id);
}