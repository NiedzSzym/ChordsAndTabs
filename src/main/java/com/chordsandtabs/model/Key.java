package com.chordsandtabs.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Key {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long keyId;

    private String name;

    @Enumerated(EnumType.STRING)
    private Mode mode;
}
