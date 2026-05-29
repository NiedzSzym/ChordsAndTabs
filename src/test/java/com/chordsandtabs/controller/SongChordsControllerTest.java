package com.chordsandtabs.controller;

import com.chordsandtabs.dto.chord.ChordSelectDto;
import com.chordsandtabs.dto.songChords.SongChordsCreateRequest;
import com.chordsandtabs.dto.songChords.SongChordsDto;
import com.chordsandtabs.dto.songChords.SongChordsListDto;
import com.chordsandtabs.exception.ResourceNotFoundException;
import com.chordsandtabs.model.*;
import com.chordsandtabs.repository.*;
import com.chordsandtabs.service.CurrentUserService;
import com.chordsandtabs.specification.SongChordsSpecification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SongChordsControllerTest {

    @Mock
    private SongChordsRepository songChordsRepository;

    @Mock
    private ChordRepository chordRepository;

    @Mock
    private SongRepository songRepository;

    @Mock
    private TuningRepository tuningRepository;

    @Mock
    private InstrumentTypeRepository instrumentTypeRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private KeyRepository keyRepository;

    @InjectMocks
    private SongChordsController controller;

    private Account createAccount(Long id, String email, String roleName) {
        Role role = new Role();
        role.setName(roleName);
        Account account = new Account();
        account.setAccountId(id);
        account.setEmail(email);
        account.setRole(role);
        return account;
    }

    private Song createSong(Long id, String name) {
        Song song = new Song();
        song.setSongId(id);
        song.setName(name);
        return song;
    }

    private Tuning createTuning(Long id, String name) {
        Tuning tuning = new Tuning();
        tuning.setTuningId(id);
        tuning.setTuning(name);
        return tuning;
    }

    private InstrumentType createInstrumentType(Long id, String name) {
        InstrumentType it = new InstrumentType();
        it.setInstrumentTypeId(id);
        it.setName(name);
        return it;
    }

    private Key createKey(Long id, String name) {
        Key key = new Key();
        key.setKeyId(id);
        key.setName(name);
        key.setMode(Mode.MAJOR);
        return key;
    }

    private AccountProfile createProfile(Account account, String nickname) {
        AccountProfile profile = new AccountProfile();
        profile.setAccountId(account.getAccountId());
        profile.setAccount(account);
        profile.setNickname(nickname);
        return profile;
    }

    private SongChords createSongChords(Long id, Song song, Account createdBy, Account author) {
        SongChords sc = new SongChords();
        sc.setSongChordsId(id);
        sc.setSong(song);
        sc.setCreatedBy(createdBy);
        sc.setAuthor(author);
        sc.setStatus(Status.PUBLIC);
        sc.setNotationType(NotationType.CHORDS);
        sc.setKey(createKey(1L, "C"));
        sc.setTuning(createTuning(1L, "Standard"));
        sc.setInstrumentType(createInstrumentType(1L, "Guitar"));
        sc.setCreatedAt(OffsetDateTime.now());
        return sc;
    }

    @Test
    void getAll_shouldReturnSongChordsForSong() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test Song");
        SongChords sc = createSongChords(1L, song, user, user);
        sc.setAuthor(user);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(songChordsRepository.findAll(any(Specification.class))).thenReturn(List.of(sc));

        List<SongChordsListDto> result = controller.getAll(1L, null, null, null);

        assertEquals(1, result.size());
        assertEquals("Test Song", result.get(0).songName());
    }

    @Test
    void getAll_shouldFilterByNotationType() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test Song");
        SongChords sc = createSongChords(1L, song, user, user);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(songChordsRepository.findAll(any(Specification.class))).thenReturn(List.of(sc));

        List<SongChordsListDto> result = controller.getAll(1L, NotationType.CHORDS, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void getAll_shouldFilterByTuning() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test Song");
        SongChords sc = createSongChords(1L, song, user, user);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(songChordsRepository.findAll(any(Specification.class))).thenReturn(List.of(sc));

        List<SongChordsListDto> result = controller.getAll(1L, null, 1L, null);

        assertEquals(1, result.size());
    }

    @Test
    void getAll_shouldFilterByInstrumentType() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test Song");
        SongChords sc = createSongChords(1L, song, user, user);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(songChordsRepository.findAll(any(Specification.class))).thenReturn(List.of(sc));

        List<SongChordsListDto> result = controller.getAll(1L, null, null, 1L);

        assertEquals(1, result.size());
    }

    @Test
    void getAll_shouldApplyAccessibleByFilter() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test Song");

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(songChordsRepository.findAll(any(Specification.class))).thenReturn(List.of());

        List<SongChordsListDto> result = controller.getAll(1L, null, null, null);

        assertEquals(0, result.size());
    }

    @Test
    void getSongChords_shouldReturnDto() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test Song");
        SongChords sc = createSongChords(1L, song, user, user);

        when(songChordsRepository.findById(1L)).thenReturn(Optional.of(sc));
        when(currentUserService.canModify(user)).thenReturn(true);

        ResponseEntity<SongChordsDto> response = controller.getSongChords(1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Song", response.getBody().songName());
    }

    @Test
    void getSongChords_shouldReturn403_whenNotOwner() {
        Account owner = createAccount(1L, "owner@test.pl", "ROLE_USER");
        Account other = createAccount(2L, "other@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test Song");
        SongChords sc = createSongChords(1L, song, owner, owner);

        when(songChordsRepository.findById(1L)).thenReturn(Optional.of(sc));
        when(currentUserService.canModify(owner)).thenReturn(false);

        ResponseEntity<SongChordsDto> response = controller.getSongChords(1L, 1L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getSongChords_shouldThrow404_whenNotFound() {
        when(songChordsRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> controller.getSongChords(1L, 999L));
    }

    @Test
    void getSongChords_shouldThrow404_whenSongIdMismatch() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Song song = createSong(2L, "Other Song");
        SongChords sc = createSongChords(1L, song, user, user);

        when(songChordsRepository.findById(1L)).thenReturn(Optional.of(sc));

        assertThrows(ResourceNotFoundException.class, () -> controller.getSongChords(1L, 1L));
    }

    @Test
    void createSongChords_shouldReturn201() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test Song");
        SongChordsCreateRequest request = new SongChordsCreateRequest(
                1L, 1L, 1L, "CHORDS", "PUBLIC",
                null, null, null, null, null, null
        );

        when(songRepository.findById(1L)).thenReturn(Optional.of(song));
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(keyRepository.findById(1L)).thenReturn(Optional.of(createKey(1L, "C")));
        when(tuningRepository.findById(1L)).thenReturn(Optional.of(createTuning(1L, "Standard")));
        when(instrumentTypeRepository.findById(1L)).thenReturn(Optional.of(createInstrumentType(1L, "Guitar")));
        when(songChordsRepository.save(any())).thenAnswer(invocation -> {
            SongChords sc = invocation.getArgument(0);
            sc.setSongChordsId(100L);
            return sc;
        });

        ResponseEntity<Void> response = controller.createSongChords(1L, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getHeaders().getLocation().toString().contains("/api/songs/1/chords/100"));
        verify(songChordsRepository).save(any());
    }

    @Test
    void createSongChords_shouldSaveWithChordIds() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test Song");
        SongChordsCreateRequest request = new SongChordsCreateRequest(
                1L, 1L, 1L, "CHORDS", "PUBLIC",
                null, null, null, null, "Song body", List.of(1L, 2L)
        );

        Chord chord1 = new Chord();
        chord1.setChordId(1L);
        Chord chord2 = new Chord();
        chord2.setChordId(2L);

        when(songRepository.findById(1L)).thenReturn(Optional.of(song));
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(keyRepository.findById(1L)).thenReturn(Optional.of(createKey(1L, "C")));
        when(tuningRepository.findById(1L)).thenReturn(Optional.of(createTuning(1L, "Standard")));
        when(instrumentTypeRepository.findById(1L)).thenReturn(Optional.of(createInstrumentType(1L, "Guitar")));
        when(chordRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(chord1, chord2));
        when(songChordsRepository.save(any())).thenAnswer(invocation -> {
            SongChords sc = invocation.getArgument(0);
            sc.setSongChordsId(100L);
            return sc;
        });

        controller.createSongChords(1L, request);

        ArgumentCaptor<SongChords> captor = ArgumentCaptor.forClass(SongChords.class);
        verify(songChordsRepository).save(captor.capture());
        SongChords saved = captor.getValue();
        assertEquals("Song body", saved.getSongBody());
        assertEquals(2, saved.getChords().size());
    }

    @Test
    void createSongChords_shouldThrow404_whenSongNotFound() {
        SongChordsCreateRequest request = new SongChordsCreateRequest(
                1L, 1L, 1L, null, null,
                null, null, null, null, null, null
        );

        when(songRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> controller.createSongChords(999L, request));
        verify(songChordsRepository, never()).save(any());
    }

    @Test
    void updateSongChords_shouldReturn204() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test Song");
        SongChords sc = createSongChords(1L, song, user, user);
        SongChordsCreateRequest request = new SongChordsCreateRequest(
                1L, 1L, 1L, "TABS", "PRIVATE",
                "down-down", "4/4", 120, 2, "Updated body", null
        );

        when(songChordsRepository.findById(1L)).thenReturn(Optional.of(sc));
        when(currentUserService.canModify(user)).thenReturn(true);
        when(keyRepository.findById(1L)).thenReturn(Optional.of(createKey(1L, "C")));
        when(tuningRepository.findById(1L)).thenReturn(Optional.of(createTuning(1L, "Standard")));
        when(instrumentTypeRepository.findById(1L)).thenReturn(Optional.of(createInstrumentType(1L, "Guitar")));

        ResponseEntity<Void> response = controller.updateSongChords(1L, 1L, request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("TABS", sc.getNotationType().name());
        assertEquals("PRIVATE", sc.getStatus().name());
        assertEquals("down-down", sc.getStrummingPattern());
        assertEquals("4/4", sc.getTimeSignature());
        assertEquals(120, sc.getTempo());
        assertEquals(2, sc.getCapoFret());
        assertEquals("Updated body", sc.getSongBody());
        assertNotNull(sc.getUpdatedAt());
        verify(songChordsRepository).save(sc);
    }

    @Test
    void updateSongChords_shouldReturn403_whenNotOwner() {
        Account owner = createAccount(1L, "owner@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test Song");
        SongChords sc = createSongChords(1L, song, owner, owner);
        SongChordsCreateRequest request = new SongChordsCreateRequest(
                1L, 1L, 1L, null, null,
                null, null, null, null, null, null
        );

        when(songChordsRepository.findById(1L)).thenReturn(Optional.of(sc));
        when(currentUserService.canModify(owner)).thenReturn(false);

        ResponseEntity<Void> response = controller.updateSongChords(1L, 1L, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(songChordsRepository, never()).save(any());
    }

    @Test
    void updateSongChords_shouldThrow404_whenNotFound() {
        SongChordsCreateRequest request = new SongChordsCreateRequest(
                1L, 1L, 1L, null, null,
                null, null, null, null, null, null
        );

        when(songChordsRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> controller.updateSongChords(1L, 999L, request));
    }

    @Test
    void updateSongChords_shouldThrow404_whenSongIdMismatch() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Song song = createSong(2L, "Other Song");
        SongChords sc = createSongChords(1L, song, user, user);
        SongChordsCreateRequest request = new SongChordsCreateRequest(
                1L, 1L, 1L, null, null,
                null, null, null, null, null, null
        );

        when(songChordsRepository.findById(1L)).thenReturn(Optional.of(sc));

        assertThrows(ResourceNotFoundException.class, () -> controller.updateSongChords(1L, 1L, request));
    }

    @Test
    void deleteSongChords_shouldReturn204() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test Song");
        SongChords sc = createSongChords(1L, song, user, user);

        when(songChordsRepository.findById(1L)).thenReturn(Optional.of(sc));
        when(currentUserService.canModify(user)).thenReturn(true);

        ResponseEntity<Void> response = controller.deleteSongChords(1L, 1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNotNull(sc.getDeletedAt());
        verify(songChordsRepository).save(sc);
    }

    @Test
    void deleteSongChords_shouldReturn403_whenNotOwner() {
        Account owner = createAccount(1L, "owner@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test Song");
        SongChords sc = createSongChords(1L, song, owner, owner);

        when(songChordsRepository.findById(1L)).thenReturn(Optional.of(sc));
        when(currentUserService.canModify(owner)).thenReturn(false);

        ResponseEntity<Void> response = controller.deleteSongChords(1L, 1L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(songChordsRepository, never()).save(any());
    }

    @Test
    void deleteSongChords_shouldThrow404_whenNotFound() {
        when(songChordsRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> controller.deleteSongChords(1L, 999L));
    }

    @Test
    void deleteSongChords_shouldThrow404_whenSongIdMismatch() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Song song = createSong(2L, "Other Song");
        SongChords sc = createSongChords(1L, song, user, user);

        when(songChordsRepository.findById(1L)).thenReturn(Optional.of(sc));

        assertThrows(ResourceNotFoundException.class, () -> controller.deleteSongChords(1L, 1L));
    }

    @Test
    void createSongChords_shouldUseDefaultStatus_whenNull() {
        Account user = createAccount(1L, "user@test.pl", "ROLE_USER");
        Song song = createSong(1L, "Test Song");
        SongChordsCreateRequest request = new SongChordsCreateRequest(
                1L, 1L, 1L, null, null,
                null, null, null, null, null, null
        );

        when(songRepository.findById(1L)).thenReturn(Optional.of(song));
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(keyRepository.findById(1L)).thenReturn(Optional.of(createKey(1L, "C")));
        when(tuningRepository.findById(1L)).thenReturn(Optional.of(createTuning(1L, "Standard")));
        when(instrumentTypeRepository.findById(1L)).thenReturn(Optional.of(createInstrumentType(1L, "Guitar")));
        when(songChordsRepository.save(any())).thenAnswer(invocation -> {
            SongChords sc = invocation.getArgument(0);
            sc.setSongChordsId(100L);
            return sc;
        });

        controller.createSongChords(1L, request);

        ArgumentCaptor<SongChords> captor = ArgumentCaptor.forClass(SongChords.class);
        verify(songChordsRepository).save(captor.capture());
        assertEquals(Status.PUBLIC, captor.getValue().getStatus());
    }
}
