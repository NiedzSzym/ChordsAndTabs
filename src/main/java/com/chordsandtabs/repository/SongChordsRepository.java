package com.chordsandtabs.repository;

import com.chordsandtabs.model.SongChords;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SongChordsRepository extends CrudRepository<SongChords, Long>, JpaSpecificationExecutor<SongChords> {
    @NonNull
    List<SongChords> findAll(@NonNull Specification<SongChords> spec);
}
