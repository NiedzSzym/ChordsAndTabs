package com.chordsandtabs.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;


@Entity
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    private String email;

    private char[] password;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToOne(mappedBy = "account")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private AccountProfile profile;

    private OffsetDateTime createdAt;

    private OffsetDateTime  updatedAt;

    private OffsetDateTime deletedAt;

    private OffsetDateTime emailVerifiedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

}
