package com.chordsandtabs.controller;

import com.chordsandtabs.dto.song.SongCreateRequest;
import com.chordsandtabs.dto.song.SongDto;
import com.chordsandtabs.model.Account;
import com.chordsandtabs.model.Artist;
import com.chordsandtabs.model.Role;
import com.chordsandtabs.model.Song;
import com.chordsandtabs.repository.ArtistRepository;
import com.chordsandtabs.repository.SongRepository;
import com.chordsandtabs.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SongControllerTest {

    @Mock
    private SongRepository songRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private SongController controller;

    private Account createAccount(Long id, String email, String roleName) {
        Role role = new Role();
        role.setName(roleName);
        Account account = new Account();
        account.setAccountId(id);
        account.setEmail(email);
        account.setRole(role);
        return account;
    }

    private Artist createArtist(Long id, String name) {
        Artist artist = new Artist();
        artist.setArtistId(id);
        artist.setName(name);
        return artist;
    }

    private Song createSong(Long id, String name, Account createdBy) {
        Song song = new Song();
        song.setSongId(id);
        song.setName(name);
        song.setReleaseYear(2024);
        song.setCreatedBy(createdBy);
        Artist artist = createArtist(1L, "Test Artist");
        song.setArtists(new java.util.HashSet<>(Set.of(artist)));
        return song;
    }

    @Test
    void getAll_shouldReturnPageWithCreatedBy() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test Song", user);
        Page<Song> page = new PageImpl<>(List.of(song));

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(songRepository.findAll(any(), any(Pageable.class))).thenReturn(page);

        Page<SongDto> result = controller.getAll(PageRequest.of(0, 20), null, null, null, null);

        assertEquals(1, result.getTotalElements());
        SongDto dto = result.getContent().get(0);
        assertEquals("Test Song", dto.name());
        assertEquals("user@test.pl", dto.createdBy());
    }

    @Test
    void getAll_shouldReturnNullCreatedBy_whenSongHasNoCreator() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test Song", null);
        Page<Song> page = new PageImpl<>(List.of(song));

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(songRepository.findAll(any(), any(Pageable.class))).thenReturn(page);

        Page<SongDto> result = controller.getAll(PageRequest.of(0, 20), null, null, null, null);

        assertNull(result.getContent().get(0).createdBy());
    }

    @Test
    void getAll_withMySongsTrue_shouldFilterByCurrentUser() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Song song = createSong(1L, "My Song", user);
        Page<Song> page = new PageImpl<>(List.of(song));

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(songRepository.findAll(any(), any(Pageable.class))).thenReturn(page);

        Page<SongDto> result = controller.getAll(PageRequest.of(0, 20), null, null, null, true);

        assertEquals(1, result.getTotalElements());
        assertEquals("My Song", result.getContent().get(0).name());
    }

    @Test
    void getAll_withMySongsFalse_shouldNotFilterByCurrentUser() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Account admin = createAccount(2L, "admin@test.pl", "ROLE_ADMIN");
        Song userSong = createSong(1L, "User Song", user);
        Song adminSong = createSong(2L, "Admin Song", admin);
        Page<Song> page = new PageImpl<>(List.of(userSong, adminSong));

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(songRepository.findAll(any(), any(Pageable.class))).thenReturn(page);

        Page<SongDto> result = controller.getAll(PageRequest.of(0, 20), null, null, null, false);

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void getSong_shouldReturnSongWithCreatedBy() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test Song", user);

        when(songRepository.findByIdWithArtists(1L)).thenReturn(Optional.of(song));
        when(currentUserService.canModify(user)).thenReturn(true);

        ResponseEntity<SongDto> response = controller.getSong(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        SongDto dto = response.getBody();
        assertEquals("Test Song", dto.name());
        assertEquals("user@test.pl", dto.createdBy());
    }

    @Test
    void getSong_shouldReturn404_whenNotFound() {
        when(songRepository.findByIdWithArtists(999L)).thenReturn(Optional.empty());

        ResponseEntity<SongDto> response = controller.getSong(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getSong_shouldReturn404_whenNotOwner() {
        Account owner = createAccount(1L, "owner@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test Song", owner);

        when(songRepository.findByIdWithArtists(1L)).thenReturn(Optional.of(song));
        when(currentUserService.canModify(owner)).thenReturn(false);

        ResponseEntity<SongDto> response = controller.getSong(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createSong_shouldReturn201WithLocation() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        SongCreateRequest request = new SongCreateRequest("New Song", 2024, List.of(1L));

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findAllById(List.of(1L))).thenReturn(List.of(createArtist(1L, "Test Artist")));
        when(songRepository.save(any())).thenAnswer(invocation -> {
            Song song = invocation.getArgument(0);
            song.setSongId(100L);
            return song;
        });

        ResponseEntity<Void> response = controller.createSong(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getHeaders().getLocation().toString().contains("/api/songs/100"));
        verify(songRepository).save(any());
    }

    @Test
    void createSong_shouldSaveWithCurrentUserAsCreator() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        SongCreateRequest request = new SongCreateRequest("New Song", 2024, null);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(songRepository.save(any())).thenAnswer(invocation -> {
            Song song = invocation.getArgument(0);
            song.setSongId(100L);
            return song;
        });

        controller.createSong(request);

        ArgumentCaptor<Song> captor = ArgumentCaptor.forClass(Song.class);
        verify(songRepository).save(captor.capture());
        assertEquals("user@test.pl", captor.getValue().getCreatedBy().getEmail());
    }

    @Test
    void updateSong_shouldReturn204_whenOwner() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Old Name", user);
        SongCreateRequest request = new SongCreateRequest("Updated Name", 2025, null);

        when(songRepository.findById(1L)).thenReturn(Optional.of(song));
        when(currentUserService.canModify(user)).thenReturn(true);

        ResponseEntity<Void> response = controller.updateSong(1L, request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("Updated Name", song.getName());
        assertEquals(2025, song.getReleaseYear());
    }

    @Test
    void updateSong_shouldReturn403_whenNotOwner() {
        Account owner = createAccount(1L, "owner@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test", owner);
        SongCreateRequest request = new SongCreateRequest("Updated", 2024, null);

        when(songRepository.findById(1L)).thenReturn(Optional.of(song));
        when(currentUserService.canModify(owner)).thenReturn(false);

        ResponseEntity<Void> response = controller.updateSong(1L, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateSong_shouldReturn404_whenNotFound() {
        SongCreateRequest request = new SongCreateRequest("Updated", 2024, null);

        when(songRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = controller.updateSong(999L, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteSong_shouldReturn204AndSoftDelete_whenOwner() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test", user);

        when(songRepository.findById(1L)).thenReturn(Optional.of(song));
        when(currentUserService.canModify(user)).thenReturn(true);

        ResponseEntity<Void> response = controller.deleteSong(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNotNull(song.getDeletedAt());
        verify(songRepository).save(song);
    }

    @Test
    void deleteSong_shouldReturn403_whenNotOwner() {
        Account owner = createAccount(1L, "owner@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test", owner);

        when(songRepository.findById(1L)).thenReturn(Optional.of(song));
        when(currentUserService.canModify(owner)).thenReturn(false);

        ResponseEntity<Void> response = controller.deleteSong(1L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(songRepository, never()).save(any());
    }

    @Test
    void deleteSong_shouldReturn404_whenNotFound() {
        when(songRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = controller.deleteSong(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
