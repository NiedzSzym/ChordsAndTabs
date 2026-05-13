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
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("api/songs/{songId}/chords")
public class SongChordsController {
    SongChordsRepository songChordsRepository;
    ChordRepository chordRepository;
    SongRepository songRepository;
    TuningRepository tuningRepository;
    InstrumentTypeRepository instrumentTypeRepository;
    ArtistRepository artistRepository;
    CurrentUserService currentUserService;
    KeyRepository keyRepository;

    public SongChordsController(SongChordsRepository songChordsRepository,
                                ChordRepository chordRepository,
                                SongRepository songRepository,
                                TuningRepository tuningRepository,
                                InstrumentTypeRepository instrumentTypeRepository,
                                ArtistRepository artistRepository,
                                CurrentUserService currentUserService,
                                KeyRepository keyRepository) {
        this.songChordsRepository = songChordsRepository;
        this.chordRepository = chordRepository;
        this.songRepository = songRepository;
        this.tuningRepository = tuningRepository;
        this.instrumentTypeRepository = instrumentTypeRepository;
        this.artistRepository = artistRepository;
        this.currentUserService = currentUserService;
        this.keyRepository = keyRepository;
    }

    @GetMapping
    @Cacheable(value = "songChords", key = "@currentUserService.getCurrentUser().getAccountId() + '-' + #songId")
    public List<SongChordsListDto> getAll(
            @PathVariable Long songId,
            @RequestParam(required = false) NotationType notationType,
            @RequestParam(required = false) Long tuningId,
            @RequestParam(required = false) Long instrumentTypeId
    ) {
        Specification<SongChords> spec = Specification.where(SongChordsSpecification.hasSong(songId));
        if (notationType != null) spec = spec.and(SongChordsSpecification.hasNotationType(notationType));
        if (tuningId != null) spec = spec.and(SongChordsSpecification.hasTuningId(tuningId));
        if (instrumentTypeId != null) spec = spec.and(SongChordsSpecification.hasInstrumentTypeId(instrumentTypeId));
        spec = spec.and(SongChordsSpecification.accessibleBy(currentUserService.getCurrentUser()));
        return songChordsRepository.findAll(spec).stream().map(this::toListDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongChordsDto> getSongChords(
            @PathVariable Long songId,
            @PathVariable Long id
    ) {
        SongChords songChords = songChordsRepository.findById(id)
                .filter(sc -> sc.getSong().getSongId().equals(songId))
                .orElseThrow(() -> new ResourceNotFoundException("SongChord", id));

        if (!currentUserService.canModify(songChords.getCreatedBy())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(toDto(songChords));

    }

    @PostMapping
    @CacheEvict(value = "songChords", key = "@currentUserService.getCurrentUser().getAccountId() + '-' + #songId")
    public ResponseEntity<Void> createSongChords(
            @PathVariable Long songId,
            @RequestBody @Valid SongChordsCreateRequest req
    ) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song", songId));
        Account currentUser = currentUserService.getCurrentUser();
        SongChords sc = new SongChords();
        sc.setSong(song);
        sc.setAuthor(currentUser);
        sc.setCreatedBy(currentUser);
        sc.setStatus(req.status() != null ? Status.valueOf(req.status()) : Status.PUBLIC);
        sc.setNotationType(req.notationType() != null ? NotationType.valueOf(req.notationType()) : null);
        sc.setKey(keyRepository.findById(req.keyId()).orElseThrow(() -> new ResourceNotFoundException("Key", req.keyId())));
        sc.setTuning(tuningRepository.findById(req.tuningId()).orElseThrow(() -> new ResourceNotFoundException("Tuning", req.tuningId())));
        sc.setInstrumentType(instrumentTypeRepository.findById(req.instrumentTypeId()).orElseThrow(() -> new ResourceNotFoundException("Instrument", req.instrumentTypeId())));
        sc.setStrummingPattern(req.strummingPattern());
        sc.setTimeSignature(req.timeSignature());
        sc.setTempo(req.tempo());
        sc.setCapoFret(req.capoFret());
        sc.setSongBody(req.songBody());
        if (req.chordIds() != null) {
            Set<Chord> chords = new HashSet<>();
            chordRepository.findAllById(req.chordIds()).forEach(chords::add);
            sc.setChords(chords);
        }
        songChordsRepository.save(sc);
        return ResponseEntity.created(URI.create(
                "/api/songs/" + songId + "/chords/" + sc.getSongChordsId()
        )).build();
    }

    @PutMapping("/{id}")
    @CacheEvict(value = "songChords", key = "@currentUserService.getCurrentUser().getAccountId() + '-' + #songId")
    public ResponseEntity<Void> updateSongChords(
            @PathVariable Long songId,
            @PathVariable Long id,
            @RequestBody @Valid SongChordsCreateRequest req
    ) {
        SongChords sc = songChordsRepository.findById(id)
                .filter(s -> s.getSong().getSongId().equals(songId))
                .orElseThrow(() -> new ResourceNotFoundException("SongChords", id));

        if (!currentUserService.canModify(sc.getCreatedBy())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        sc.setKey(keyRepository.findById(req.keyId())
                .orElseThrow(() -> new ResourceNotFoundException("Key", req.keyId())));
        sc.setTuning(tuningRepository.findById(req.tuningId())
                .orElseThrow(() -> new ResourceNotFoundException("Tuning", req.tuningId())));
        sc.setInstrumentType(instrumentTypeRepository.findById(req.instrumentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Instrument", req.instrumentTypeId())));

        if (req.status() != null) sc.setStatus(Status.valueOf(req.status()));
        if (req.notationType() != null) sc.setNotationType(NotationType.valueOf(req.notationType()));
        if (req.strummingPattern() != null) sc.setStrummingPattern(req.strummingPattern());
        if (req.timeSignature() != null) sc.setTimeSignature(req.timeSignature());
        if (req.tempo() != null) sc.setTempo(req.tempo());
        if (req.capoFret() != null) sc.setCapoFret(req.capoFret());
        if (req.songBody() != null) sc.setSongBody(req.songBody());

        if (req.chordIds() != null) {
            Set<Chord> chords = new HashSet<>();
            chordRepository.findAllById(req.chordIds()).forEach(chords::add);
            sc.setChords(chords);
        }

        sc.setUpdatedAt(OffsetDateTime.now());
        songChordsRepository.save(sc);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "songChords", key = "@currentUserService.getCurrentUser().getAccountId() + '-' + #songId")
    public ResponseEntity<Void> deleteSongChords(
            @PathVariable Long songId,
            @PathVariable Long id
    ) {
        SongChords sc = songChordsRepository.findById(id)
                .filter(s -> s.getSong().getSongId().equals(songId))
                .orElseThrow(() -> new ResourceNotFoundException("SongChords", id));

        if (!currentUserService.canModify(sc.getCreatedBy())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        sc.setDeletedAt(OffsetDateTime.now());
        songChordsRepository.save(sc);
        return ResponseEntity.noContent().build();
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

    private SongChordsDto toDto(SongChords sc) {
        return new SongChordsDto(
                sc.getSongChordsId(),
                sc.getSong().getName(),
                sc.getKey() != null ? sc.getKey().getName() : null,
                sc.getTuning() != null ? sc.getTuning().getTuning() : null,
                sc.getInstrumentType() != null ? sc.getInstrumentType().getName() : null,
                sc.getStatus() != null ? sc.getStatus().name() : null,
                sc.getNotationType() != null ? sc.getNotationType().name() : null,
                sc.getAuthor() != null && sc.getAuthor().getProfile() != null
                        ? sc.getAuthor().getProfile().getNickname() : null,
                sc.getCreatedAt(),
                sc.getUpdatedAt(),
                sc.getStrummingPattern(),
                sc.getTimeSignature(),
                sc.getTempo(),
                sc.getCapoFret(),
                sc.getSongBody(),
                sc.getChords().stream()
                        .map(c -> new ChordSelectDto(c.getChordId(), c.getName(), c.getChordFingering()))
                        .toList()
        );
    }
}
