package com.chordsandtabs.controller;

import com.chordsandtabs.model.InstrumentType;
import com.chordsandtabs.repository.InstrumentTypeRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentTypeController {
    private final InstrumentTypeRepository instrumentTypeRepository;

    public InstrumentTypeController(InstrumentTypeRepository instrumentTypeRepository) {
        this.instrumentTypeRepository = instrumentTypeRepository;
    }

    @GetMapping
    public List<InstrumentType> findAllByOrderByNameAsc() {
        return instrumentTypeRepository.findAllByOrderByNameAsc();
    }
}
