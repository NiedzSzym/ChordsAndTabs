package com.chordsandtabs.model;

import jakarta.persistence.*;
import lombok.Data;


import java.time.OffsetDateTime;

@Entity
@Data
public class AccountProfile {
    @Id
    private Long accountId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private String nickname;

    private String bio;

    private OffsetDateTime  createdAt;

    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}
