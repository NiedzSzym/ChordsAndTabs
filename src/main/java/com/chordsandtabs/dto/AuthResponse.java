package com.chordsandtabs.dto;

public record AuthResponse(String token) {
    public AuthResponse(String token) {
        this.token = token;
    }
}
