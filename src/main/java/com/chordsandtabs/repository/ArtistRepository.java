package com.chordsandtabs.repository;

import com.chordsandtabs.model.Artist;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ArtistRepository extends CrudRepository<Artist, Long> {
    List<Artist> findAllByOrderByNameAsc();
}
