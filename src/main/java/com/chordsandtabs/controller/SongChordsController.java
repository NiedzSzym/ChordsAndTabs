package com.chordsandtabs.controller;


import com.chordsandtabs.dto.songChords.SongChordsListDto;
import com.chordsandtabs.model.NotationType;
import com.chordsandtabs.model.SongChords;
import com.chordsandtabs.model.Tuning;
import com.chordsandtabs.repository.*;
import com.chordsandtabs.specification.SongChordsSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/songs/{songId}/chords")
public class SongChordsController {
    SongChordsRepository songChordsRepository;
    ChordRepository chordRepository;
    SongRepository songRepository;
    TuningRepository tuningRepository;
    InstrumentTypeRepository instrumentTypeRepository;
    ArtistRepository artistRepository;

    public SongChordsController(SongChordsRepository songChordsRepository,
                                ChordRepository chordRepository,
                                SongRepository songRepository,
                                TuningRepository tuningRepository,
                                InstrumentTypeRepository instrumentTypeRepository,
                                ArtistRepository artistRepository) {
        this.songChordsRepository = songChordsRepository;
        this.chordRepository = chordRepository;
        this.songRepository = songRepository;
        this.tuningRepository = tuningRepository;
        this.instrumentTypeRepository = instrumentTypeRepository;
        this.artistRepository = artistRepository;
    }

    @GetMapping
    List<SongChordsListDto> getAll(
            @PathVariable Long songId,
            @RequestParam(required = false) NotationType notationType,
            @RequestParam(required = false) Long tuningId,
            @RequestParam(required = false) Long instrumentTypeId
    ) {
        Specification<SongChords> spec = Specification.where(SongChordsSpecification.hasSong(songId));
        if (notationType != null) spec = spec.and(SongChordsSpecification.hasNotationType(notationType));
        if (tuningId != null) spec = spec.and(SongChordsSpecification.hasTuningId(tuningId));
        if (instrumentTypeId != null) spec = spec.and(SongChordsSpecification.hasInstrumentTypeId(instrumentTypeId));
        return songChordsRepository.findAll(spec).stream().map(this::toListDto).toList();
    }

    private SongChordsListDto toListDto(SongChords songChords) {
        return new SongChordsListDto(
                songChords.getSongChordsId(),
                songChords.getSong().getName(),
                songChords.getKey() != null ? songChords.getKey().getName() : null,
                songChords.getTuning() != null ? songChords.getTuning().getTuning() : null,
                songChords.getInstrumentType() != null ? songChords.getInstrumentType().getName() : null,
                songChords.getNotationType() != null ? songChords.getNotationType().name() : null,
                songChords.getAuthor() != null && songChords.getAuthor().getProfile() != null
                        ? songChords.getAuthor().getProfile().getNickname() : null,
                songChords.getCreatedAt()
        );
    }
}
