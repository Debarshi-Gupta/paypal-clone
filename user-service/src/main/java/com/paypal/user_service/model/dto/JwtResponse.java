package com.paypal.user_service.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {

    private String token;
    private String tokenType; // "Bearer"
    private String email;
    private String role;
    private long expiresIn;
}
