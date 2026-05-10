package com.chordsandtabs.dto.chord;

public record ChordListDto(
        Long chordId,
        String name,
        String chordFingering,
        String instrumentTypeName,
        String tuningName,
        String createdBy
) { }
