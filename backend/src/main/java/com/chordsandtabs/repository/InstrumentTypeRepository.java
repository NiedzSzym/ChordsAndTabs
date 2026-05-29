package com.chordsandtabs.repository;

import com.chordsandtabs.model.InstrumentType;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InstrumentTypeRepository extends CrudRepository<InstrumentType, Long> {
    List<InstrumentType> findAllByOrderByNameAsc();
}
