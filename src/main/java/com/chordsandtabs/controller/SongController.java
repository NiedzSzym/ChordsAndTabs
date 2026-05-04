package com.chordsandtabs.controller;


import com.chordsandtabs.dto.song.SongsListDto;
import com.chordsandtabs.model.Artist;
import com.chordsandtabs.repository.SongRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongRepository songRepository;

    public SongController(SongRepository songRepository) {
        this.songRepository = songRepository;
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
}
