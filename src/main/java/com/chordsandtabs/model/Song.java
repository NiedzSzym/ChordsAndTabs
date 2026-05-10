package com.chordsandtabs.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long songId;

    String name;

    Integer releaseYear;

    @ManyToMany
    @JoinTable(
            name = "artist_song",
            joinColumns = @JoinColumn(name = "song_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    Set<Artist> artists = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Account createdBy;

    @SQLRestriction("deleted_at IS NULL")
    private OffsetDateTime deletedAt;
}
