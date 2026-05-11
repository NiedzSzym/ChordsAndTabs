package com.chordsandtabs.dto.security;

public record AuthResponse(String token) {
    public AuthResponse(String token) {
        this.token = token;
    }
}
