package com.chordsandtabs.security;

import com.chordsandtabs.model.Account;
import com.chordsandtabs.repository.AccountRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomAccountDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;

    public CustomAccountDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        Account account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(account.getEmail())
                .password(String.valueOf(account.getPassword()))
                .authorities(account.getRole().getName())
                .build();
    }
}
