package com.chordsandtabs.dto.chord;

import com.chordsandtabs.validation.ValidChordFingering;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@ValidChordFingering
public record ChordCreateRequest(
        @NotBlank String name,
        @NotNull Long instrumentTypeId,
        @NotNull Long tuningId,
        @NotBlank String chordFingering
) {
}
