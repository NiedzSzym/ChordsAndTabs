package com.chordsandtabs.repository;

import com.chordsandtabs.model.InstrumentType;
import com.chordsandtabs.model.Tuning;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TuningRepository extends CrudRepository<Tuning, Long> {

    List<Tuning> findAllByTuningId(Long tuningId);

    List<Tuning> findAllByInstrumentType(InstrumentType instrumentType);

    List<Tuning> findAllByInstrumentType_InstrumentTypeId(Long instrumentTypeInstrumentTypeId);
}
