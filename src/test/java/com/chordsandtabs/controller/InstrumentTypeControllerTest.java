package com.chordsandtabs.controller;

import com.chordsandtabs.model.InstrumentType;
import com.chordsandtabs.repository.InstrumentTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstrumentTypeControllerTest {

    @Mock
    private InstrumentTypeRepository instrumentTypeRepository;

    @InjectMocks
    private InstrumentTypeController controller;

    @Test
    void findAllByOrderByNameAsc_shouldReturnAllInstruments() {
        InstrumentType guitar = new InstrumentType();
        guitar.setInstrumentTypeId(1L);
        guitar.setName("Guitar");
        guitar.setStringCount(6);

        InstrumentType bass = new InstrumentType();
        bass.setInstrumentTypeId(2L);
        bass.setName("Bass");
        bass.setStringCount(4);

        when(instrumentTypeRepository.findAllByOrderByNameAsc()).thenReturn(List.of(bass, guitar));

        List<InstrumentType> result = controller.findAllByOrderByNameAsc();

        assertEquals(2, result.size());
        assertEquals("Bass", result.get(0).getName());
        assertEquals("Guitar", result.get(1).getName());
    }

    @Test
    void findAllByOrderByNameAsc_shouldReturnEmptyList_whenNoInstruments() {
        when(instrumentTypeRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

        List<InstrumentType> result = controller.findAllByOrderByNameAsc();

        assertEquals(0, result.size());
    }
}
