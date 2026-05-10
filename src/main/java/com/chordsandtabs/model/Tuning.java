package com.chordsandtabs.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;

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

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Account createdBy;

    @SQLRestriction("deleted_at IS NULL")
    private OffsetDateTime deletedAt;
}
