package com.chordsandtabs.dto.songChords;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SongChordsCreateRequest(
        @NotNull Long keyId,
        @NotNull Long tuningId,
        @NotNull Long instrumentTypeId,
        String notationType,
        String status,
        String strummingPattern,
        String timeSignature,
        Integer tempo,
        Integer capoFret,
        String songBody,
        List<Long> chordIds
) { }
