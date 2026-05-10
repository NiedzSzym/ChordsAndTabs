package com.chordsandtabs.service;


import com.chordsandtabs.model.Account;
import com.chordsandtabs.repository.AccountRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final AccountRepository accountRepository;

    public CurrentUserService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new IllegalStateException("No authenticated user");
        }
        String email = auth.getName();
        if (email == null) {
            throw new IllegalStateException("Authentication principal has no email");
        }
        return accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}
