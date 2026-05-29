package com.chordsandtabs.controller;

import com.chordsandtabs.dto.tuning.TuningCreateRequest;
import com.chordsandtabs.exception.ResourceNotFoundException;
import com.chordsandtabs.model.*;
import com.chordsandtabs.repository.AccountRepository;
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
class TuningControllerTest {

    @Mock
    private TuningRepository tuningRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private InstrumentTypeRepository instrumentTypeRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private TuningController controller;

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
        return it;
    }

    private Tuning createTuning(Long id, String name, Account createdBy) {
        Tuning tuning = new Tuning();
        tuning.setTuningId(id);
        tuning.setTuning(name);
        tuning.setInstrumentType(createInstrumentType(1L, "Guitar"));
        tuning.setCreatedBy(createdBy);
        return tuning;
    }

    @Test
    void getAll_shouldReturnAllTunings_whenNoFilter() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Tuning tuning = createTuning(1L, "Standard", user);

        when(tuningRepository.findAll()).thenReturn(List.of(tuning));
        when(currentUserService.canModify(user)).thenReturn(true);

        List<Tuning> result = controller.getAll(null);

        assertEquals(1, result.size());
        assertEquals("Standard", result.get(0).getTuning());
    }

    @Test
    void getAll_shouldFilterByInstrumentType() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Tuning tuning = createTuning(1L, "Standard", user);

        when(tuningRepository.findAllByInstrumentType_InstrumentTypeId(1L)).thenReturn(List.of(tuning));
        when(currentUserService.canModify(user)).thenReturn(true);

        List<Tuning> result = controller.getAll(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getAll_shouldFilterInaccessibleTunings() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Account other = createAccount(2L, "other@test.pl", "ROLE_USER");
        Tuning tuning1 = createTuning(1L, "Standard", user);
        Tuning tuning2 = createTuning(2L, "Drop D", other);

        when(tuningRepository.findAll()).thenReturn(List.of(tuning1, tuning2));
        when(currentUserService.canModify(user)).thenReturn(true);
        when(currentUserService.canModify(other)).thenReturn(false);

        List<Tuning> result = controller.getAll(null);

        assertEquals(1, result.size());
        assertEquals("Standard", result.get(0).getTuning());
    }

    @Test
    void getAll_shouldIncludeTuningsWithNullCreatedBy() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Tuning systemTuning = createTuning(1L, "Standard", null);

        when(tuningRepository.findAll()).thenReturn(List.of(systemTuning));

        List<Tuning> result = controller.getAll(null);

        assertEquals(1, result.size());
    }

    @Test
    void createTuning_shouldReturn201() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        TuningCreateRequest request = new TuningCreateRequest("Standard", 1L);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(instrumentTypeRepository.findById(1L)).thenReturn(Optional.of(createInstrumentType(1L, "Guitar")));
        when(tuningRepository.save(any())).thenAnswer(invocation -> {
            Tuning tuning = invocation.getArgument(0);
            tuning.setTuningId(1L);
            return tuning;
        });

        ResponseEntity<Void> response = controller.createTuning(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(tuningRepository).save(any());
    }

    @Test
    void createTuning_shouldThrow404_whenInstrumentTypeNotFound() {
        TuningCreateRequest request = new TuningCreateRequest("Standard", 999L);

        when(instrumentTypeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> controller.createTuning(request));
        verify(tuningRepository, never()).save(any());
    }

    @Test
    void deleteTuning_shouldReturn204_whenOwner() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Tuning tuning = createTuning(1L, "Standard", user);

        when(tuningRepository.findById(1L)).thenReturn(Optional.of(tuning));
        when(currentUserService.canModify(user)).thenReturn(true);

        ResponseEntity<Void> response = controller.deleteTuning(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNotNull(tuning.getDeletedAt());
        verify(tuningRepository).save(tuning);
    }

    @Test
    void deleteTuning_shouldReturn403_whenNotOwner() {
        Account owner = createAccount(1L, "owner@test.pl", "ROLE_USER");
        Tuning tuning = createTuning(1L, "Standard", owner);

        when(tuningRepository.findById(1L)).thenReturn(Optional.of(tuning));
        when(currentUserService.canModify(owner)).thenReturn(false);

        ResponseEntity<Void> response = controller.deleteTuning(1L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(tuningRepository, never()).save(any());
    }

    @Test
    void deleteTuning_shouldReturn404_whenNotFound() {
        when(tuningRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = controller.deleteTuning(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(tuningRepository, never()).save(any());
    }
}
