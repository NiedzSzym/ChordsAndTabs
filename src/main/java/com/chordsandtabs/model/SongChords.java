package com.chordsandtabs.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class SongChords {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long songChordsId;

    @ManyToOne
    @JoinColumn(name = "song_id")
    private Song song;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private Account author;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
    
    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private NotationType notationType;

    @ManyToOne
    @JoinColumn(name = "key_id")
    private Key key;

    @ManyToOne
    @JoinColumn(name = "tuning_id")
    private Tuning tuning;

    @ManyToOne
    @JoinColumn(name = "instrument_type_id")
    private InstrumentType instrumentType;

    private String strummingPattern;

    private String timeSignature;

    private Integer tempo;

    private Integer capoFret;

    private String songBody;

    @ManyToMany
    @JoinTable(
            name = "song_chords_chord",
            joinColumns = @JoinColumn(name = "song_chords_id"),
            inverseJoinColumns = @JoinColumn(name = "chord_id")
    )
    private Set<Chord> chords = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Account createdBy;

}
