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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;


@RestController
@RequestMapping("/api/songs")
@Transactional(readOnly = true)
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
    @Cacheable(value = "songs", key = "@currentUserService.getCurrentUser().getAccountId() + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #artist + '-' + #year + '-' + #name + '-' + #mySongs")
    public Page<SongDto> getAll(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean mySongs
    ) {

        Specification<Song> spec = Specification.unrestricted();
        if (artist != null) spec = spec.and(SongSpecification.hasArtist(artist));
        if (year != null) spec = spec.and(SongSpecification.hasYear(year));
        if (name != null) spec = spec.and(SongSpecification.hasNameLike(name));
        spec = spec.and(SongSpecification.accessibleBy(currentUserService.getCurrentUser()));
        if (Boolean.TRUE.equals(mySongs)) {
            spec = spec.and(SongSpecification.hasCreatedBy(currentUserService.getCurrentUser()));
        }

        Page<Song> page = songRepository.findAll(spec, pageable);

        return page.map(this::toDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongDto> getSong(
            @PathVariable Long id
    ) {
        return songRepository.findByIdWithArtists(id)
                .filter(a -> a.getCreatedBy() == null || currentUserService.canModify(a.getCreatedBy()))
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "songs", allEntries = true),
            @CacheEvict(value = "songById", allEntries = true)
    })
    public ResponseEntity<Void> createSong(@RequestBody @Valid SongCreateRequest req) {
        Song song = new Song();
        song.setName(req.name());
        song.setReleaseYear(req.releaseYear());
        song.setCreatedBy(currentUserService.getCurrentUser());

        findArtistWithID(req, song);
        return ResponseEntity.created(URI.create("/api/songs/" + song.getSongId())).build();
    }



    @PutMapping("/{id}")
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "songs", allEntries = true),
            @CacheEvict(value = "songById", allEntries = true)
    })
    public ResponseEntity<Void> updateSong(@PathVariable Long id,@RequestBody @Valid SongCreateRequest req) {
        Optional<Song> existing = songRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Song song = existing.get();

        if (!currentUserService.canModify(song.getCreatedBy())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

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
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "songs", allEntries = true),
            @CacheEvict(value = "songById", allEntries = true)
    })
    public ResponseEntity<Void> deleteSong(@PathVariable Long id ) {
        Optional<Song> existing = songRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Song song = existing.get();
        if (!currentUserService.canModify(song.getCreatedBy())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        song.setDeletedAt(OffsetDateTime.now());
        songRepository.save(song);
        return ResponseEntity.noContent().build();
    }

    private SongDto toDto(Song song) {
        return new SongDto(
                song.getSongId(),
                song.getName(),
                song.getReleaseYear(),
                song.getArtists().stream()
                        .map(Artist::getName)
                        .toList(),
                song.getCreatedBy() != null ? song.getCreatedBy().getEmail() : null
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
