package com.chordsandtabs.repository;

import com.chordsandtabs.model.Artist;
import org.springframework.data.repository.CrudRepository;

public interface ArtistRepository extends CrudRepository<Artist, Long> {
}
