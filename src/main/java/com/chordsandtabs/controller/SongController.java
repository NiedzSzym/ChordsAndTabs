package com.chordsandtabs.controller;


import com.chordsandtabs.dto.song.SongCreateRequest;
import com.chordsandtabs.dto.song.SongsListDto;
import com.chordsandtabs.model.Artist;
import com.chordsandtabs.model.Song;
import com.chordsandtabs.repository.ArtistRepository;
import com.chordsandtabs.repository.SongRepository;
import jakarta.validation.Valid;
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
    public List<SongsListDto> getAll() {
        return songRepository.findAllWithArtists().stream()
                .map(song -> new SongsListDto(
                        song.getSong_id(),
                        song.getName(),
                        song.getArtists().stream()
                                .map(Artist::getName)
                                .toList()
                ))
                .toList();
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
}
