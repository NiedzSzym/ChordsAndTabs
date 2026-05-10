package com.chordsandtabs.dto.tuning;

import com.chordsandtabs.model.InstrumentType;
import jakarta.validation.constraints.NotBlank;
import org.aspectj.weaver.ast.Not;

public record TuningCreateRequest(
        @NotBlank
        String tuning,
        @NotBlank
        Long instrumentTypeId
) {
}
