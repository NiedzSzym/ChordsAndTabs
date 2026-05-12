package com.chordsandtabs.repository;

import com.chordsandtabs.model.SongChords;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SongChordsRepository extends CrudRepository<SongChords, Long>, JpaSpecificationExecutor<SongChords> {
    @Override
    @Cacheable(value = "songChordsById", key = "#id")
    Optional<SongChords> findById(@NonNull Long id);

    @NonNull
    List<SongChords> findAll(@NonNull Specification<SongChords> spec);
}
