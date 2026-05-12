package com.chordsandtabs.controller;


import com.chordsandtabs.dto.song.SongCreateRequest;
import com.chordsandtabs.dto.song.SongDto;
import com.chordsandtabs.model.Artist;
import com.chordsandtabs.model.Song;
import com.chordsandtabs.repository.ArtistRepository;
import com.chordsandtabs.repository.SongRepository;
import com.chordsandtabs.service.CurrentUserService;
import com.chordsandtabs.specification.SongSpecification;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;


@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final CurrentUserService currentUserService;

    public SongController(SongRepository songRepository,
                          ArtistRepository artistRepository,
                          CurrentUserService currentUserService) {
        this.songRepository = songRepository;
        this.artistRepository = artistRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public Page<SongDto> getAll(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String name
    ) {

        Specification<Song> spec = Specification.unrestricted();
        if (artist != null) spec = spec.and(SongSpecification.hasArtist(artist));
        if (year != null) spec = spec.and(SongSpecification.hasYear(year));
        if (name != null) spec = spec.and(SongSpecification.hasNameLike(name));

        Page<Song> page = songRepository.findAll(
                spec, pageable
        );

        return page.map(this::toDto);
    }

    @GetMapping("/{id}")
    @Cacheable(value = "songs", key = "#id")
    public ResponseEntity<SongDto> getSong(
            @PathVariable Long id
    ) {
        return songRepository.findByIdWithArtists(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    @CacheEvict(value = "songs", allEntries = true)
    public ResponseEntity<Void> createSong(@RequestBody @Valid SongCreateRequest req) {
        Song song = new Song();
        song.setName(req.name());
        song.setReleaseYear(req.releaseYear());
        song.setCreatedBy(currentUserService.getCurrentUser());

        findArtistWithID(req, song);
        return ResponseEntity.created(URI.create("/api/songs/" + song.getSongId())).build();
    }



    @PutMapping("/{id}")
    @CacheEvict(value = "songs", allEntries = true)
    public ResponseEntity<Void> updateSong(@PathVariable Long id,@RequestBody @Valid SongCreateRequest req) {
        Optional<Song> existing = songRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Song song = existing.get();
        if (req.name() != null) {
            song.setName(req.name());
        }
        if (req.releaseYear() != null) {
            song.setReleaseYear(req.releaseYear());
        }

        findArtistWithID(req, song);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "songs", allEntries = true)
    public ResponseEntity<Void> deleteSong(@PathVariable Long id ) {
        Optional<Song> song = songRepository.findById(id);
        if (song.isEmpty()) return ResponseEntity.notFound().build();
        Song s = song.get();
        s.setDeletedAt(OffsetDateTime.now());
        songRepository.save(s);
        return ResponseEntity.noContent().build();
    }

    private SongDto toDto(Song song) {
        return new SongDto(
                song.getSongId(),
                song.getName(),
                song.getReleaseYear(),
                song.getArtists().stream()
                        .map(Artist::getName)
                        .toList()
        );
    }

    private void findArtistWithID(@RequestBody @Valid SongCreateRequest req, Song song) {
        if (req.artistIds() != null) {
            var artists = artistRepository.findAllById(req.artistIds());
            HashSet<Artist> artistsSet = new HashSet<>();
            artists.forEach(artistsSet::add);
            song.setArtists(artistsSet);
        }

        songRepository.save(song);
    }
}
