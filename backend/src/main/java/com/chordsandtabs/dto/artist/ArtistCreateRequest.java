package com.chordsandtabs.dto.artist;

import jakarta.validation.constraints.NotBlank;

public record ArtistCreateRequest(
        @NotBlank String name
) { }
