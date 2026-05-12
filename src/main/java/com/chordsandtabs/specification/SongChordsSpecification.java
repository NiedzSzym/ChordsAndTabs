package com.chordsandtabs.specification;

import com.chordsandtabs.model.Account;
import com.chordsandtabs.model.NotationType;
import com.chordsandtabs.model.SongChords;
import org.springframework.data.jpa.domain.Specification;

public class SongChordsSpecification {
    public static Specification<SongChords> hasNotationType(NotationType notationType) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("notationType"), notationType);
        }

    public static Specification<SongChords> hasTuningId(Long tuningId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("tuningId").get("tuningId"), tuningId);
    }

    public static Specification<SongChords> hasInstrumentTypeId(Long instrumentTypeId) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("instrumentType").get("instrumentTypeId"), instrumentTypeId);
    }

    public static Specification<SongChords> hasSong(Long songId) {
        return (root, query, cb) -> cb.equal(root.get("song").get("songId"), songId);
    }

    public static Specification<SongChords> accessibleBy(Account user) {
        return (root, query, cb) -> {
            if ("ROLE_ADMIN".equals(user.getRole().getName())) return cb.conjunction();
            return cb.or(
                    cb.equal(root.get("createdBy").get("accountId"), user.getAccountId()),
                    cb.equal(root.get("createdBy").get("role").get("name"), "ROLE_ADMIN")
            );
        };
    }
}
