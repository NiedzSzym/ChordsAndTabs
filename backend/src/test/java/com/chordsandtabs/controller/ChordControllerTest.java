package com.chordsandtabs.controller;

import com.chordsandtabs.dto.chord.ChordCreateRequest;
import com.chordsandtabs.dto.chord.ChordListDto;
import com.chordsandtabs.dto.chord.ChordSelectDto;
import com.chordsandtabs.exception.ResourceNotFoundException;
import com.chordsandtabs.model.*;
import com.chordsandtabs.repository.ChordRepository;
import com.chordsandtabs.repository.InstrumentTypeRepository;
import com.chordsandtabs.repository.TuningRepository;
import com.chordsandtabs.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChordControllerTest {

    @Mock
    private ChordRepository chordRepository;

    @Mock
    private InstrumentTypeRepository instrumentTypeRepository;

    @Mock
    private TuningRepository tuningRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ChordController controller;

    private Account createAccount(Long id, String email, String roleName) {
        Role role = new Role();
        role.setName(roleName);
        Account account = new Account();
        account.setAccountId(id);
        account.setEmail(email);
        account.setRole(role);
        return account;
    }

    private InstrumentType createInstrumentType(Long id, String name) {
        InstrumentType it = new InstrumentType();
        it.setInstrumentTypeId(id);
        it.setName(name);
        it.setStringCount(6);
        return it;
    }

    private Tuning createTuning(Long id, String name) {
        Tuning tuning = new Tuning();
        tuning.setTuningId(id);
        tuning.setTuning(name);
        return tuning;
    }

    private Chord createChord(Long id, String name, String fingering, Account createdBy) {
        Chord chord = new Chord();
        chord.setChordId(id);
        chord.setName(name);
        chord.setChordFingering(fingering);
        chord.setInstrumentType(createInstrumentType(1L, "Guitar"));
        chord.setTuning(createTuning(1L, "Standard"));
        chord.setCreatedBy(createdBy);
        return chord;
    }

    @Test
    void getAll_shouldReturnAllAccessibleChords() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Chord chord = createChord(1L, "C", "0-0-0-0-0-0", user);

        when(chordRepository.findAll()).thenReturn(List.of(chord));
        when(currentUserService.canModify(user)).thenReturn(true);

        List<ChordListDto> result = controller.getAll();

        assertEquals(1, result.size());
        assertEquals("C", result.get(0).name());
    }

    @Test
    void getAll_shouldFilterInaccessibleChords() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Account other = createAccount(2L, "other@test.pl", "ROLE_USER");
        Chord chord1 = createChord(1L, "C", "0-0-0-0-0-0", user);
        Chord chord2 = createChord(2L, "D", "0-0-0-0-0-0", other);

        when(chordRepository.findAll()).thenReturn(List.of(chord1, chord2));
        when(currentUserService.canModify(user)).thenReturn(true);
        when(currentUserService.canModify(other)).thenReturn(false);

        List<ChordListDto> result = controller.getAll();

        assertEquals(1, result.size());
        assertEquals("C", result.get(0).name());
    }

    @Test
    void getSelect_shouldReturnFilteredChords() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Chord chord = createChord(1L, "C", "0-0-0-0-0-0", user);

        when(chordRepository.findByTuning_TuningIdAndInstrumentType_InstrumentTypeId(1L, 1L))
                .thenReturn(List.of(chord));
        when(currentUserService.canModify(user)).thenReturn(true);

        List<ChordSelectDto> result = controller.getSelect(1L, 1L);

        assertEquals(1, result.size());
        assertEquals("C", result.get(0).name());
    }

    @Test
    void createChord_shouldReturn201() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        ChordCreateRequest request = new ChordCreateRequest("C", 1L, 1L, "0-0-0-0-0-0");

        when(instrumentTypeRepository.findById(1L)).thenReturn(Optional.of(createInstrumentType(1L, "Guitar")));
        when(tuningRepository.findById(1L)).thenReturn(Optional.of(createTuning(1L, "Standard")));
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(chordRepository.save(any())).thenAnswer(invocation -> {
            Chord chord = invocation.getArgument(0);
            chord.setChordId(100L);
            return chord;
        });

        ResponseEntity<Void> response = controller.createChord(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getHeaders().getLocation().toString().contains("/api/chords/100"));
        verify(chordRepository).save(any());
    }

    @Test
    void createChord_shouldThrow404_whenInstrumentTypeNotFound() {
        ChordCreateRequest request = new ChordCreateRequest("C", 999L, 1L, "0-0-0-0-0-0");

        when(instrumentTypeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> controller.createChord(request));
        verify(chordRepository, never()).save(any());
    }

    @Test
    void createChord_shouldThrow404_whenTuningNotFound() {
        ChordCreateRequest request = new ChordCreateRequest("C", 1L, 999L, "0-0-0-0-0-0");

        when(instrumentTypeRepository.findById(1L)).thenReturn(Optional.of(createInstrumentType(1L, "Guitar")));
        when(tuningRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> controller.createChord(request));
        verify(chordRepository, never()).save(any());
    }

    @Test
    void deleteChord_shouldReturn204_whenOwner() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Chord chord = createChord(1L, "C", "0-0-0-0-0-0", user);

        when(chordRepository.findById(1L)).thenReturn(Optional.of(chord));
        when(currentUserService.canModify(user)).thenReturn(true);

        ResponseEntity<Void> response = controller.deleteChord(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNotNull(chord.getDeletedAt());
        verify(chordRepository).save(chord);
    }

    @Test
    void deleteChord_shouldReturn403_whenNotOwner() {
        Account owner = createAccount(1L, "owner@test.pl", "ROLE_USER");
        Chord chord = createChord(1L, "C", "0-0-0-0-0-0", owner);

        when(chordRepository.findById(1L)).thenReturn(Optional.of(chord));
        when(currentUserService.canModify(owner)).thenReturn(false);

        ResponseEntity<Void> response = controller.deleteChord(1L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(chordRepository, never()).save(any());
    }

    @Test
    void deleteChord_shouldReturn404_whenNotFound() {
        when(chordRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = controller.deleteChord(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(chordRepository, never()).save(any());
    }
}
