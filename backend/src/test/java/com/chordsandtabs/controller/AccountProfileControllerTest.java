package com.chordsandtabs.controller;

import com.chordsandtabs.dto.profile.AccountProfileDto;
import com.chordsandtabs.dto.profile.AccountProfileUpdateRequest;
import com.chordsandtabs.exception.ResourceNotFoundException;
import com.chordsandtabs.model.Account;
import com.chordsandtabs.model.AccountProfile;
import com.chordsandtabs.model.Role;
import com.chordsandtabs.repository.AccountProfileRepository;
import com.chordsandtabs.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountProfileControllerTest {

    @Mock
    private AccountProfileRepository accountProfileRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AccountProfileController controller;

    private Account createAccount(Long id, String email) {
        Role role = new Role();
        role.setName("ROLE_USER");
        Account account = new Account();
        account.setAccountId(id);
        account.setEmail(email);
        account.setRole(role);
        return account;
    }

    private AccountProfile createProfile(Long id, String nickname, String bio, Account account) {
        AccountProfile profile = new AccountProfile();
        profile.setAccountId(id);
        profile.setAccount(account);
        profile.setNickname(nickname);
        profile.setBio(bio);
        profile.setCreatedAt(OffsetDateTime.now());
        return profile;
    }

    @Test
    void getMyProfile_shouldReturnProfile_whenExists() {
        Account user = createAccount(1L, "user@test.pl");
        AccountProfile profile = createProfile(1L, "john", "Bio here", user);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(accountProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

        ResponseEntity<AccountProfileDto> response = controller.getMyProfile();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("john", response.getBody().nickname());
        assertEquals("Bio here", response.getBody().bio());
    }

    @Test
    void getMyProfile_shouldReturn404_whenNoProfile() {
        Account user = createAccount(1L, "user@test.pl");

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(accountProfileRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<AccountProfileDto> response = controller.getMyProfile();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getProfile_shouldReturnProfile() {
        Account user = createAccount(1L, "user@test.pl");
        AccountProfile profile = createProfile(1L, "john", "Bio", user);

        when(accountProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

        ResponseEntity<AccountProfileDto> response = controller.getProfile(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("john", response.getBody().nickname());
    }

    @Test
    void getProfile_shouldReturn404_whenNotFound() {
        when(accountProfileRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<AccountProfileDto> response = controller.getProfile(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createProfile_shouldReturn201() {
        Account user = createAccount(1L, "user@test.pl");
        AccountProfileUpdateRequest request = new AccountProfileUpdateRequest("john", "My bio");

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(accountProfileRepository.findById(1L)).thenReturn(Optional.empty());
        when(accountProfileRepository.save(any())).thenAnswer(invocation -> {
            AccountProfile profile = invocation.getArgument(0);
            if (profile.getAccountId() == null && profile.getAccount() != null) {
                profile.setAccountId(profile.getAccount().getAccountId());
            }
            return profile;
        });

        ResponseEntity<Void> response = controller.createProfile(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getHeaders().getLocation().toString().contains("/api/profile/1"));
        verify(accountProfileRepository).save(any());
    }

    @Test
    void createProfile_shouldReturn400_whenProfileAlreadyExists() {
        Account user = createAccount(1L, "user@test.pl");
        AccountProfile existing = createProfile(1L, "existing", "bio", user);
        AccountProfileUpdateRequest request = new AccountProfileUpdateRequest("john", "My bio");

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(accountProfileRepository.findById(1L)).thenReturn(Optional.of(existing));

        ResponseEntity<Void> response = controller.createProfile(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(accountProfileRepository, never()).save(any());
    }

    @Test
    void updateProfile_shouldReturn204() {
        Account user = createAccount(1L, "user@test.pl");
        AccountProfile profile = createProfile(1L, "old", "old bio", user);
        AccountProfileUpdateRequest request = new AccountProfileUpdateRequest("new", "new bio");

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(accountProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

        ResponseEntity<Void> response = controller.updateProfile(request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("new", profile.getNickname());
        assertEquals("new bio", profile.getBio());
        assertNotNull(profile.getUpdatedAt());
        verify(accountProfileRepository).save(profile);
    }

    @Test
    void updateProfile_shouldThrow404_whenNoProfile() {
        Account user = createAccount(1L, "user@test.pl");
        AccountProfileUpdateRequest request = new AccountProfileUpdateRequest("new", "bio");

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(accountProfileRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> controller.updateProfile(request));
        verify(accountProfileRepository, never()).save(any());
    }
}
