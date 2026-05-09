package com.chordsandtabs.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class InstrumentType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long instrumentTypeId;

    private String name;

    private Integer stringCount;

}
