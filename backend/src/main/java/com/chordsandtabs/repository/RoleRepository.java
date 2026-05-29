package com.chordsandtabs.repository;

import com.chordsandtabs.model.Role;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
