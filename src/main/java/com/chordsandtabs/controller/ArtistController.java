package com.chordsandtabs.controller;

import com.chordsandtabs.dto.artist.ArtistCreateRequest;
import com.chordsandtabs.dto.artist.ArtistDto;
import com.chordsandtabs.exception.ResourceNotFoundException;
import com.chordsandtabs.model.Artist;
import com.chordsandtabs.repository.ArtistRepository;
import com.chordsandtabs.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {
    private final ArtistRepository artistRepository;
    private final CurrentUserService currentUserService;

    public ArtistController(ArtistRepository artistRepository,
                            CurrentUserService currentUserService) {
        this.artistRepository = artistRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    @Cacheable("artists")
    public List<ArtistDto> getAll() {
        return artistRepository.findAllByOrderByNameAsc()
                .stream()
                .map(a -> new ArtistDto(a.getArtistId(), a.getName()))
                .toList();
    }

    @GetMapping("/{id}")
    @Cacheable(value = "artists", key = "#id")
    public ResponseEntity<ArtistDto> getArtist(@PathVariable Long id) {
        return artistRepository.findById(id)
                .map(a -> new ArtistDto(a.getArtistId(), a.getName()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @CacheEvict(value = "artists", allEntries = true)
    public ResponseEntity<Void> createArtist(@RequestBody @Valid ArtistCreateRequest req) {
        Artist artist = new Artist();
        artist.setName(req.name());
        artist.setCreatedBy(currentUserService.getCurrentUser());
        artistRepository.save(artist);
        return ResponseEntity.created(URI.create("/api/artists/" + artist.getArtistId())).build();
    }

    @PutMapping("/{id}")
    @CacheEvict(value = "artists", allEntries = true)
    public ResponseEntity<Void> updateArtist(@PathVariable Long id,
                                      @RequestBody @Valid ArtistCreateRequest req) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artist", id));
        artist.setName(req.name());
        artistRepository.save(artist);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "artists", allEntries = true)
    public ResponseEntity<Void> deleteArtist(@PathVariable Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artist", id));
        artist.setDeletedAt(OffsetDateTime.now());
        artistRepository.save(artist);
        return ResponseEntity.noContent().build();
    }
}
