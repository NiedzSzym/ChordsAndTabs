package com.chordsandtabs.repository;

import com.chordsandtabs.model.Chord;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ChordRepository extends CrudRepository<Chord, Long> {
    @Override
    @Cacheable(value = "chordById", key = "#id")
    Optional<Chord> findById(@NonNull Long id);

    List<Chord> findByTuning_TuningIdAndInstrumentType_InstrumentTypeId(Long tuningId, Long instrumentTypeId);
}
