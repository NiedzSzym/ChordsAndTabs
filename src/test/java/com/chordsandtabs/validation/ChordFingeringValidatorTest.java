package com.chordsandtabs.validation;

import com.chordsandtabs.dto.chord.ChordCreateRequest;
import com.chordsandtabs.model.InstrumentType;
import com.chordsandtabs.repository.InstrumentTypeRepository;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChordFingeringValidatorTest {

    @Mock
    private InstrumentTypeRepository instrumentTypeRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConstraintValidatorContext context;

    @InjectMocks
    private ChordFingeringValidator validator;

    private InstrumentType createInstrument(int stringCount) {
        InstrumentType type = new InstrumentType();
        type.setInstrumentTypeId(1L);
        type.setName("Test Instrument");
        type.setStringCount(stringCount);
        return type;
    }

    @Test
    void shouldAcceptValidSixStringFingering() {
        when(instrumentTypeRepository.findById(1L)).thenReturn(Optional.of(createInstrument(6)));
        var req = new ChordCreateRequest("C", 1L, 1L, "1-3-3-2-1-1");
        assertTrue(validator.isValid(req, context));
    }

    @Test
    void shouldAcceptValidFourStringFingering() {
        when(instrumentTypeRepository.findById(1L)).thenReturn(Optional.of(createInstrument(4)));
        var req = new ChordCreateRequest("C", 1L, 1L, "0-2-2-0");
        assertTrue(validator.isValid(req, context));
    }

    @Test
    void shouldRejectTooFewSegments() {
        when(instrumentTypeRepository.findById(1L)).thenReturn(Optional.of(createInstrument(6)));
        var req = new ChordCreateRequest("C", 1L, 1L, "1-3-3-2-1");

        assertFalse(validator.isValid(req, context));
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(contains("6 segments"));
    }

    @Test
    void shouldRejectTooManySegments() {
        when(instrumentTypeRepository.findById(1L)).thenReturn(Optional.of(createInstrument(4)));
        var req = new ChordCreateRequest("C", 1L, 1L, "0-2-2-0-0");

        assertFalse(validator.isValid(req, context));
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(contains("4 segments"));
    }

    @Test
    void shouldRejectNonIntegerSegment() {
        when(instrumentTypeRepository.findById(1L)).thenReturn(Optional.of(createInstrument(6)));
        var req = new ChordCreateRequest("C", 1L, 1L, "1-3-x-2-1-1");

        assertFalse(validator.isValid(req, context));
        verify(context).disableDefaultConstraintViolation();
    }

    @Test
    void shouldRejectNegativeSegment() {
        when(instrumentTypeRepository.findById(1L)).thenReturn(Optional.of(createInstrument(6)));
        var req = new ChordCreateRequest("C", 1L, 1L, "1-3--1-2-1-1");

        assertFalse(validator.isValid(req, context));
        verify(context).disableDefaultConstraintViolation();
    }

    @Test
    void shouldAcceptNullInstrumentTypeId() {
        var req = new ChordCreateRequest("C", null, 1L, "1-3-3-2-1-1");
        assertTrue(validator.isValid(req, context));
        verifyNoInteractions(instrumentTypeRepository);
    }

    @Test
    void shouldAcceptNullChordFingering() {
        var req = new ChordCreateRequest("C", 1L, 1L, null);
        assertTrue(validator.isValid(req, context));
        verifyNoInteractions(instrumentTypeRepository);
    }

    @Test
    void shouldAcceptNonExistentInstrumentType() {
        when(instrumentTypeRepository.findById(999L)).thenReturn(Optional.empty());
        var req = new ChordCreateRequest("C", 999L, 1L, "1-3-3-2-1-1");
        assertTrue(validator.isValid(req, context));
    }

    @Test
    void shouldBuildCustomViolationForWrongSegmentCount() {
        when(instrumentTypeRepository.findById(1L)).thenReturn(Optional.of(createInstrument(6)));
        var req = new ChordCreateRequest("C", 1L, 1L, "1-3-3-2-1");

        validator.isValid(req, context);

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(contains("6 segments"));
        verify(context.buildConstraintViolationWithTemplate(anyString())).addPropertyNode("chordFingering");
    }
}
