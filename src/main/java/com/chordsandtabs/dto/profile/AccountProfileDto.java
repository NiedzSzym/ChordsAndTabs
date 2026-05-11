package com.chordsandtabs.dto.profile;

import java.time.OffsetDateTime;

public record AccountProfileDto(
        Long accountId,
        String nickname,
        String bio,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) { }
