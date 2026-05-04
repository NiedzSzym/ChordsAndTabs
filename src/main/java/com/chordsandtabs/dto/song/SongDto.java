package com.chordsandtabs.dto.song;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record SongDto(
        Long id,
        String name,
        Integer year,
        @NotBlank
        List<String> artistNames
) {}
