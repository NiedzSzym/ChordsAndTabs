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
import org.springframework.http.HttpStatus;
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
    @Cacheable(value = "artists", key = "#currentUserService.getCurrentUser().getAccountId()")
    public List<ArtistDto> getAll() {
        return artistRepository.findAllByOrderByNameAsc()
                .stream()
                .filter(a -> a.getCreatedBy() == null || currentUserService.canModify(a.getCreatedBy()))
                .map(a -> new ArtistDto(a.getArtistId(), a.getName()))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtistDto> getArtist(@PathVariable Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artist", id));

        if (!currentUserService.canModify(artist.getCreatedBy())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ArtistDto dto = new ArtistDto(artist.getArtistId(), artist.getName());
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<Void> createArtist(@RequestBody @Valid ArtistCreateRequest req) {
        Artist artist = new Artist();
        artist.setName(req.name());
        artist.setCreatedBy(currentUserService.getCurrentUser());
        artistRepository.save(artist);
        return ResponseEntity.created(URI.create("/api/artists/" + artist.getArtistId())).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateArtist(@PathVariable Long id,
                                      @RequestBody @Valid ArtistCreateRequest req) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artist", id));

        if (!currentUserService.canModify(artist.getCreatedBy())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        artist.setName(req.name());
        artistRepository.save(artist);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArtist(@PathVariable Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artist", id));

        if (!currentUserService.canModify(artist.getCreatedBy())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        artist.setDeletedAt(OffsetDateTime.now());
        artistRepository.save(artist);
        return ResponseEntity.noContent().build();
    }
}
