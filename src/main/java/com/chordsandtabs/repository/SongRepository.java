package com.chordsandtabs.repository;

import com.chordsandtabs.model.Song;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SongRepository extends CrudRepository<Song, Long>, JpaSpecificationExecutor<Song> {
    @NonNull
    Page<Song> findAll(@NonNull Specification<Song> spec, @NonNull Pageable pageable);

    @Query("SELECT s FROM Song s LEFT JOIN FETCH s.artists WHERE s.song_id = :id")
    Optional<Song> findByIdWithArtists(@Param("id") Long id);
}
