package com.chordsandtabs.controller;


import com.chordsandtabs.dto.song.SongCreateRequest;
import com.chordsandtabs.dto.song.SongDto;
import com.chordsandtabs.model.Artist;
import com.chordsandtabs.model.Song;
import com.chordsandtabs.repository.ArtistRepository;
import com.chordsandtabs.repository.SongRepository;
import com.chordsandtabs.specification.SongSpecification;
import jakarta.validation.Valid;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashSet;
import java.util.List;


@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;

    public SongController(SongRepository songRepository, ArtistRepository artistRepository) {
        this.songRepository = songRepository;
        this.artistRepository = artistRepository;
    }

    @GetMapping
    public List<SongDto> getAll(
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String name
    ) {

        Specification<Song> spec = Specification.unrestricted();
        if (artist != null) spec = spec.and(SongSpecification.hasArtist(artist));
        if (year != null) spec = spec.and(SongSpecification.hasYear(year));
        if (name != null) spec = spec.and(SongSpecification.hasNameLike(name));

        List<Song> songs = songRepository.findAll(spec);

        return songs.stream().map(this::toDto).toList();
    }


    @PostMapping
    @RequestMapping("/create")
    public ResponseEntity<Void> create(@RequestBody @Valid SongCreateRequest req) {
        Song song = new Song();
        song.setName(req.name());
        song.setReleaseYear(req.releaseYear());

        List<Artist> artists = (List<Artist>) artistRepository.findAllById(req.artistIds());
        song.setArtists(new HashSet<>(artists));

        songRepository.save(song);
        return ResponseEntity.created(URI.create("/api/songs/create/" + song.getSong_id())).build();
    }

    @DeleteMapping
    @RequestMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id ) {
        songRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private SongDto toDto(Song song) {
        return new SongDto(
                song.getSong_id(),
                song.getName(),
                song.getReleaseYear(),
                song.getArtists().stream()
                        .map(Artist::getName)
                        .toList()
        );
    }
}
