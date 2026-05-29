package com.chordsandtabs.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@SQLRestriction("deleted_at IS NULL")
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long artistId;

    String name;

    @ManyToMany(mappedBy = "artists")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Set<Song> Songs = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Account createdBy;

    private OffsetDateTime deletedAt;
}
