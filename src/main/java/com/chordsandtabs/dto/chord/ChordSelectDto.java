package com.chordsandtabs.dto.chord;

public record ChordSelectDto(
        Long chordId,
        String name,
        String chordFingering
) {
}
