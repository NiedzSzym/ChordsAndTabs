package com.chordsandtabs.controller;

import com.chordsandtabs.dto.tuning.TuningCreateRequest;
import com.chordsandtabs.model.Tuning;
import com.chordsandtabs.repository.AccountRepository;
import com.chordsandtabs.repository.InstrumentTypeRepository;
import com.chordsandtabs.repository.TuningRepository;
import com.chordsandtabs.service.CurrentUserService;
import jakarta.validation.Valid;
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
    public List<Tuning> getAll(
            @RequestParam(required = false) Long instrumentTypeId
    ) {
        if (instrumentTypeId == null) {
            return (List<Tuning>) tuningRepository.findAll();
        }
        return tuningRepository.findAllByInstrumentType_InstrumentTypeId(instrumentTypeId);
    }

    @PostMapping
    public ResponseEntity<Void> createTuning(@RequestBody @Valid TuningCreateRequest req) {
        Tuning tuning = new Tuning();
        tuning.setCreatedBy(currentUserService.getCurrentUser());
        tuning.setTuning(req.tuning());
        tuning.setInstrumentType(
                instrumentTypeRepository.findById(req.instrumentTypeId()).orElseThrow(
                        () -> new RuntimeException("Instrument type not found: " + req.instrumentTypeId())
                )
        );

        return ResponseEntity.created(URI.create("api/tunings" + tuning.getTuningId())).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTuning(@PathVariable Long id) {
        Optional<Tuning> tuning = tuningRepository.findById(id);
        if (tuning.isEmpty()) return ResponseEntity.notFound().build();
        Tuning t = tuning.get();
        t.setDeletedAt(OffsetDateTime.now());
        tuningRepository.save(t);
        return ResponseEntity.noContent().build();
    }
}
