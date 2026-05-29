package com.chordsandtabs.controller;

import com.chordsandtabs.dto.artist.ArtistCreateRequest;
import com.chordsandtabs.dto.artist.ArtistDto;
import com.chordsandtabs.exception.ResourceNotFoundException;
import com.chordsandtabs.model.Account;
import com.chordsandtabs.model.Artist;
import com.chordsandtabs.model.Role;
import com.chordsandtabs.repository.ArtistRepository;
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
class ArtistControllerTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ArtistController controller;

    private Account createAccount(Long id, String email, String roleName) {
        Role role = new Role();
        role.setName(roleName);
        Account account = new Account();
        account.setAccountId(id);
        account.setEmail(email);
        account.setRole(role);
        return account;
    }

    private Artist createArtist(Long id, String name, Account createdBy) {
        Artist artist = new Artist();
        artist.setArtistId(id);
        artist.setName(name);
        artist.setCreatedBy(createdBy);
        return artist;
    }

    @Test
    void getAll_shouldReturnAllArtistsForAdmin() {
        Account admin = createAccount(1L, "admin@test.pl", "ROLE_ADMIN");
        Account user = createAccount(2L, "user@test.pl", "ROLE_USER");
        Artist artist1 = createArtist(1L, "Artist One", admin);
        Artist artist2 = createArtist(2L, "Artist Two", user);

        when(artistRepository.findAllByOrderByNameAsc()).thenReturn(List.of(artist1, artist2));
        when(currentUserService.canModify(admin)).thenReturn(true);
        when(currentUserService.canModify(user)).thenReturn(true);

        List<ArtistDto> result = controller.getAll();

        assertEquals(2, result.size());
    }

    @Test
    void getAll_shouldFilterArtistsBasedOnAccess() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Account other = createAccount(2L, "other@test.pl", "ROLE_USER");
        Artist accessible = createArtist(1L, "My Artist", user);
        Artist inaccessible = createArtist(2L, "Other Artist", other);

        when(artistRepository.findAllByOrderByNameAsc()).thenReturn(List.of(accessible, inaccessible));
        when(currentUserService.canModify(user)).thenReturn(true);
        when(currentUserService.canModify(other)).thenReturn(false);

        List<ArtistDto> result = controller.getAll();

        assertEquals(1, result.size());
        assertEquals("My Artist", result.get(0).name());
    }

    @Test
    void getArtist_shouldReturnArtist() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Artist artist = createArtist(1L, "Test Artist", user);

        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(currentUserService.canModify(user)).thenReturn(true);

        ResponseEntity<ArtistDto> response = controller.getArtist(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Artist", response.getBody().name());
    }

    @Test
    void getArtist_shouldReturn403_whenNotOwner() {
        Account owner = createAccount(1L, "owner@test.pl", "ROLE_USER");
        Artist artist = createArtist(1L, "Test Artist", owner);

        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(currentUserService.canModify(owner)).thenReturn(false);

        ResponseEntity<ArtistDto> response = controller.getArtist(1L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getArtist_shouldThrow404_whenNotFound() {
        when(artistRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> controller.getArtist(999L));
    }

    @Test
    void createArtist_shouldReturn201() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        ArtistCreateRequest request = new ArtistCreateRequest("New Artist");

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(artistRepository.save(any())).thenAnswer(invocation -> {
            Artist artist = invocation.getArgument(0);
            artist.setArtistId(1L);
            return artist;
        });

        ResponseEntity<Void> response = controller.createArtist(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getHeaders().getLocation().toString().contains("/api/artists/1"));
        verify(artistRepository).save(any());
    }

    @Test
    void updateArtist_shouldReturn204_whenOwner() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Artist artist = createArtist(1L, "Old Name", user);
        ArtistCreateRequest request = new ArtistCreateRequest("Updated Name");

        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(currentUserService.canModify(user)).thenReturn(true);

        ResponseEntity<Void> response = controller.updateArtist(1L, request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("Updated Name", artist.getName());
        verify(artistRepository).save(artist);
    }

    @Test
    void updateArtist_shouldReturn403_whenNotOwner() {
        Account owner = createAccount(1L, "owner@test.pl", "ROLE_USER");
        Artist artist = createArtist(1L, "Test", owner);
        ArtistCreateRequest request = new ArtistCreateRequest("Updated");

        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(currentUserService.canModify(owner)).thenReturn(false);

        ResponseEntity<Void> response = controller.updateArtist(1L, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(artistRepository, never()).save(any());
    }

    @Test
    void updateArtist_shouldThrow404_whenNotFound() {
        ArtistCreateRequest request = new ArtistCreateRequest("Updated");

        when(artistRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> controller.updateArtist(999L, request));
    }

    @Test
    void deleteArtist_shouldReturn204AndSoftDelete_whenOwner() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Artist artist = createArtist(1L, "Test", user);

        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(currentUserService.canModify(user)).thenReturn(true);

        ResponseEntity<Void> response = controller.deleteArtist(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNotNull(artist.getDeletedAt());
        verify(artistRepository).save(artist);
    }

    @Test
    void deleteArtist_shouldReturn403_whenNotOwner() {
        Account owner = createAccount(1L, "owner@test.pl", "ROLE_USER");
        Artist artist = createArtist(1L, "Test", owner);

        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(currentUserService.canModify(owner)).thenReturn(false);

        ResponseEntity<Void> response = controller.deleteArtist(1L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(artistRepository, never()).save(any());
    }

    @Test
    void deleteArtist_shouldThrow404_whenNotFound() {
        when(artistRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> controller.deleteArtist(999L));
    }
}
