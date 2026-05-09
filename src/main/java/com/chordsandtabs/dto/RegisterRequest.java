package com.chordsandtabs.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Email can't be empty")
    @Email(message = "Wrong format of Email")
    String email,
    @NotBlank
    @Size(min =8, max = 50, message = "Password must contain at least 8 characters")
    char[] password
) {}
