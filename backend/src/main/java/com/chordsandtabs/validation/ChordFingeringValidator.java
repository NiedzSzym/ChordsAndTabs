package com.chordsandtabs.validation;

import com.chordsandtabs.dto.chord.ChordCreateRequest;
import com.chordsandtabs.model.InstrumentType;
import com.chordsandtabs.repository.InstrumentTypeRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class ChordFingeringValidator implements ConstraintValidator<ValidChordFingering, ChordCreateRequest> {

    private final InstrumentTypeRepository instrumentTypeRepository;

    public ChordFingeringValidator(InstrumentTypeRepository instrumentTypeRepository) {
        this.instrumentTypeRepository = instrumentTypeRepository;
    }

    @Override
    public boolean isValid(ChordCreateRequest req, ConstraintValidatorContext context) {
        if (req.instrumentTypeId() == null || req.chordFingering() == null) {
            return true;
        }

        InstrumentType instrumentType = instrumentTypeRepository.findById(req.instrumentTypeId()).orElse(null);
        if (instrumentType == null) {
            return true;
        }

        String[] segments = req.chordFingering().split("-");
        if (segments.length != instrumentType.getStringCount()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Chord fingering must have " + instrumentType.getStringCount()
                            + " segments for " + instrumentType.getName()
            ).addPropertyNode("chordFingering").addConstraintViolation();
            return false;
        }

        for (String segment : segments) {
            try {
                int fret = Integer.parseInt(segment.trim());
                if (fret < 0) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                            "Each segment must be a non-negative integer"
                    ).addPropertyNode("chordFingering").addConstraintViolation();
                    return false;
                }
            } catch (NumberFormatException e) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "Each segment must be a valid integer"
                ).addPropertyNode("chordFingering").addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
