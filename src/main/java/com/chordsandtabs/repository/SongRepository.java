package com.chordsandtabs.repository;

import com.chordsandtabs.model.Song;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SongRepository extends CrudRepository<Song, Long>, JpaSpecificationExecutor<Song> {
    @Query("SELECT s FROM Song s LEFT JOIN FETCH s.artists")
    List<Song> findAllWithArtists();
}
