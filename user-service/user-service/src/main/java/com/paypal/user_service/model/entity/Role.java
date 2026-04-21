package com.paypal.user_service.model.entity;

public enum Role {
    ROLE_USER,
    ROLE_ADMIN;

    public String getAuthority() {
        return name();
    }
}