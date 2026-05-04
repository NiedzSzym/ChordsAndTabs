package com.chordsandtabs.dto.song;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record SongCreateRequest(
        @NotBlank
        String name,
        Integer releaseYear,
        List<Long> artistIds
) {}
