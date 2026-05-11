package com.chordsandtabs.dto.songChords;

import java.time.OffsetDateTime;

public record SongChordsListDto(
        Long songChordsId,
        String songName,
        String keyName,
        String tuningName,
        String instrumentTypeName,
        String notationType,
        String authorNickname,
        OffsetDateTime createdAt
) { }
