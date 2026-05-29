package com.chordsandtabs.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;

@Entity
@Data
@SQLRestriction("deleted_at IS NULL")
public class Tuning {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tuningId;

    private String tuning;

    @ManyToOne
    @JoinColumn(name = "instrument_type_id")
    private InstrumentType instrumentType;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Account createdBy;

    private OffsetDateTime deletedAt;
}
