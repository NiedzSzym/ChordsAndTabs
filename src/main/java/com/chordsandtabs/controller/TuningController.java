package com.chordsandtabs.controller;

import com.chordsandtabs.dto.tuning.TuningCreateRequest;
import com.chordsandtabs.exception.ResourceNotFoundException;
import com.chordsandtabs.model.Tuning;
import com.chordsandtabs.repository.AccountRepository;
import com.chordsandtabs.repository.InstrumentTypeRepository;
import com.chordsandtabs.repository.TuningRepository;
import com.chordsandtabs.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tunings")
public class TuningController {
    private final CurrentUserService currentUserService;
    TuningRepository tuningRepository;
    AccountRepository accountRepository;
    InstrumentTypeRepository instrumentTypeRepository;

    public TuningController(TuningRepository tuningRepository,
                            AccountRepository accountRepository,
                            CurrentUserService currentUserService,
                            InstrumentTypeRepository instrumentTypeRepository
    ) {
        this.tuningRepository = tuningRepository;
        this.accountRepository = accountRepository;
        this.currentUserService = currentUserService;
        this.instrumentTypeRepository = instrumentTypeRepository;
    }

    @GetMapping
    @Cacheable(value = "tunings", key = "#instrumentTypeId ?: 'all'")
    public List<Tuning> getAll(
            @RequestParam(required = false) Long instrumentTypeId
    ) {
        if (instrumentTypeId == null) {
            return (List<Tuning>) tuningRepository.findAll();
        }
        return tuningRepository.findAllByInstrumentType_InstrumentTypeId(instrumentTypeId);
    }

    @PostMapping
    @CacheEvict(value = "tunings", allEntries = true)
    public ResponseEntity<Void> createTuning(@RequestBody @Valid TuningCreateRequest req) {
        Tuning tuning = new Tuning();
        tuning.setCreatedBy(currentUserService.getCurrentUser());
        tuning.setTuning(req.tuning());
        tuning.setInstrumentType(
                instrumentTypeRepository.findById(req.instrumentTypeId()).orElseThrow(
                        () -> new ResourceNotFoundException("Instrument", req.instrumentTypeId())
                )
        );

        return ResponseEntity.created(URI.create("api/tunings" + tuning.getTuningId())).build();
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "tunings", allEntries = true)
    public ResponseEntity<Void> deleteTuning(@PathVariable Long id) {
        Optional<Tuning> tuning = tuningRepository.findById(id);
        if (tuning.isEmpty()) return ResponseEntity.notFound().build();
        Tuning t = tuning.get();
        t.setDeletedAt(OffsetDateTime.now());
        tuningRepository.save(t);
        return ResponseEntity.noContent().build();
    }
}
