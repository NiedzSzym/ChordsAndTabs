package com.chordsandtabs.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@SQLRestriction("deleted_at IS NULL")
public class Chord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chordId;

    private String name;

    @ManyToOne
    @JoinColumn(name = "instrument_type_id")
    private InstrumentType instrumentType;

    @ManyToOne
    @JoinColumn(name = "tuning_id")
    private Tuning tuning;

    private String chordFingering;

    @ManyToMany(mappedBy = "chords")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Set<SongChords> songChords = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Account createdBy;

    private OffsetDateTime deletedAt;
}
