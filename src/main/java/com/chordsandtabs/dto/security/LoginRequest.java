package com.chordsandtabs.dto.security;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank
    String email,
    @NotBlank
    char[] password
) {}
