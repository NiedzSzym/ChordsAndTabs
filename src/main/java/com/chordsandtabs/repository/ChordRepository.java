package com.chordsandtabs.repository;

import com.chordsandtabs.model.Chord;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ChordRepository extends CrudRepository<Chord, Long> {
    List<Chord> findByTuning_TuningIdAndInstrumentType_InstrumentTypeId(Long tuningId, Long instrumentTypeId);
}
