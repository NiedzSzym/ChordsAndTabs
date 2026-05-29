package com.chordsandtabs.dto.songChords;

import com.chordsandtabs.dto.chord.ChordSelectDto;

import java.time.OffsetDateTime;
import java.util.List;

public record SongChordsDto(
        Long songChordsId,
        String songName,
        String keyName,
        String tuningName,
        String instrumentTypeName,
        String status,
        String notationType,
        String authorNickname,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String strummingPattern,
        String timeSignature,
        Integer tempo,
        Integer capoFret,
        String songBody,
        List<ChordSelectDto> chords
) { }
