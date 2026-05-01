package com.chordsandtabs.repository;

import com.chordsandtabs.model.Account;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AccountRepository extends CrudRepository<Account, Long> {
    Optional<Account> findAccountByEmail(String email);
}
