package com.chordsandtabs.repository;

import com.chordsandtabs.model.Key;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface KeyRepository extends CrudRepository<Key, Long> {
    List<Key> findAllByOrderByNameAsc();
}
