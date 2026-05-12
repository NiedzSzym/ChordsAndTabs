package com.chordsandtabs.repository;

import com.chordsandtabs.model.Tuning;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TuningRepository extends CrudRepository<Tuning, Long> {
    @Override
    @Cacheable(value = "tuningById", key = "#id")
    Optional<Tuning> findById(@NonNull Long id);


    List<Tuning> findAllByInstrumentType_InstrumentTypeId(Long instrumentTypeInstrumentTypeId);
}
