package com.chordsandtabs.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Tuning {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tuningId;

    private String tuning;

    @ManyToOne
    @JoinColumn(name = "instrument_type_id")
    private InstrumentType instrumentType;

}
