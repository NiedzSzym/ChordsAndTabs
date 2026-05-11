package com.chordsandtabs.controller;

import com.chordsandtabs.dto.chord.ChordCreateRequest;
import com.chordsandtabs.dto.chord.ChordListDto;
import com.chordsandtabs.dto.chord.ChordSelectDto;
import com.chordsandtabs.exception.ResourceNotFoundException;
import com.chordsandtabs.model.Chord;
import com.chordsandtabs.model.InstrumentType;
import com.chordsandtabs.model.Tuning;
import com.chordsandtabs.repository.ChordRepository;
import com.chordsandtabs.repository.InstrumentTypeRepository;
import com.chordsandtabs.repository.TuningRepository;
import com.chordsandtabs.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chords")
public class ChordController {

    private final ChordRepository chordRepository;
    private final InstrumentTypeRepository instrumentTypeRepository;
    private final TuningRepository tuningRepository;
    private final CurrentUserService currentUserService;

    public ChordController(
            ChordRepository chordRepository,
            InstrumentTypeRepository instrumentTypeRepository,
            TuningRepository tuningRepository,
            CurrentUserService currentUserService
    ) {
        this.chordRepository = chordRepository;
        this.instrumentTypeRepository = instrumentTypeRepository;
        this.tuningRepository = tuningRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<ChordListDto> getAll() {
        var chords = new ArrayList<Chord>();
        chordRepository.findAll().forEach(chords::add);
        return chords.stream().map(this::toListDto).toList();
    }

    @GetMapping("/select")
    public List<ChordSelectDto> getSelect(
            @RequestParam Long tuningId,
            @RequestParam Long instrumentTypeId
    ) {
        return chordRepository
                .findByTuning_TuningIdAndInstrumentType_InstrumentTypeId(tuningId, instrumentTypeId)
                .stream()
                .map(this::toSelectDto)
                .toList();
    }

    @PostMapping
    public ResponseEntity<Void> createChord(@RequestBody @Valid ChordCreateRequest req) {
        InstrumentType instrumentType = instrumentTypeRepository.findById(req.instrumentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Instrument", req.instrumentTypeId()));
        Tuning tuning = tuningRepository.findById(req.tuningId())
                .orElseThrow(() -> new ResourceNotFoundException("Tuning", req.tuningId()));

        Chord chord = new Chord();
        chord.setName(req.name());
        chord.setChordFingering(req.chordFingering());
        chord.setInstrumentType(instrumentType);
        chord.setTuning(tuning);
        chord.setCreatedBy(currentUserService.getCurrentUser());

        chordRepository.save(chord);
        return ResponseEntity.created(URI.create("/api/chords/" + chord.getChordId())).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChord(@PathVariable Long id) {
        Optional<Chord> existing = chordRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Chord chord = existing.get();
        chord.setDeletedAt(OffsetDateTime.now());
        chordRepository.save(chord);
        return ResponseEntity.noContent().build();
    }

    private ChordListDto toListDto(Chord chord) {
        return new ChordListDto(
                chord.getChordId(),
                chord.getName(),
                chord.getChordFingering(),
                chord.getInstrumentType().getName(),
                chord.getTuning().getTuning(),
                chord.getCreatedBy() != null ? chord.getCreatedBy().getEmail() : null
        );
    }

    private ChordSelectDto toSelectDto(Chord chord) {
        return new ChordSelectDto(
                chord.getChordId(),
                chord.getName(),
                chord.getChordFingering()
        );
    }
}
