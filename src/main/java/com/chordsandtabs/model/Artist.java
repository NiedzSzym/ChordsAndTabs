package com.chordsandtabs.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long artistId;

    String name;

    @ManyToMany(mappedBy = "artists")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Set<Song> Songs = new HashSet<>();
}
