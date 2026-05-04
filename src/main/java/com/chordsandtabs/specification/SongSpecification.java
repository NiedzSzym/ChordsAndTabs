package com.chordsandtabs.specification;

import com.chordsandtabs.model.Artist;
import com.chordsandtabs.model.Song;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class SongSpecification {
    public static Specification<Song> hasArtist(String name) {
        return (root, query, cb) -> {
            Join<Song, Artist> artists = root.join("artists");
            return cb.equal(artists.get("name"), name);
        };
    }

    public static Specification<Song> hasYear(Integer year) {
        return (root, query, cb) ->
                cb.equal(root.get("releaseYear"), year);
    }

    public static Specification<Song> hasNameLike(String name) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }
}
