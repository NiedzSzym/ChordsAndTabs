package com.chordsandtabs.repository;

import com.chordsandtabs.model.Artist;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ArtistRepository extends CrudRepository<Artist, Long> {
    @Override
    @Cacheable(value = "artistsById", key = "#id")
    Optional<Artist> findById(@NonNull Long id);

    List<Artist> findAllByOrderByNameAsc();
}
