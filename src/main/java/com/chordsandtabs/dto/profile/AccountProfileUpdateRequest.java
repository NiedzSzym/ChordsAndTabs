package com.chordsandtabs.dto.profile;

import jakarta.validation.constraints.NotBlank;

public record AccountProfileUpdateRequest(
        @NotBlank String nickname,
        String bio
) { }
