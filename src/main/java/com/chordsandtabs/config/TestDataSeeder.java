package com.chordsandtabs.config;

import com.chordsandtabs.model.Account;
import com.chordsandtabs.model.Role;
import com.chordsandtabs.repository.AccountRepository;
import com.chordsandtabs.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class TestDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TestDataSeeder.class);

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public TestDataSeeder(AccountRepository accountRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        List<Account> allAccounts = new ArrayList<>();
        accountRepository.findAll().forEach(allAccounts::add);
        List<Account> unverified = new ArrayList<>();
        for (Account a : allAccounts) {
            if (a.getEmailVerifiedAt() == null) {
                a.setEmailVerifiedAt(OffsetDateTime.now());
                unverified.add(a);
            }
        }
        if (!unverified.isEmpty()) {
            accountRepository.saveAll(unverified);
            log.info("Verified {} existing accounts", unverified.size());
        }
        if (accountRepository.findAccountByEmail("admin@test.pl").isPresent()) {
            log.info("Test accounts already exist, skipping");
            return;
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

        String hashedPassword = passwordEncoder.encode("password123");
        Objects.requireNonNull(hashedPassword, "Encoder returned null");

        Account admin = new Account();
        admin.setEmail("admin@test.pl");
        admin.setPassword(hashedPassword.toCharArray());
        admin.setRole(adminRole);
        admin.setEmailVerifiedAt(OffsetDateTime.now());
        accountRepository.save(admin);

        Account user = new Account();
        user.setEmail("user@test.pl");
        user.setPassword(hashedPassword.toCharArray());
        user.setRole(userRole);
        accountRepository.save(user);

        log.info("Created test accounts: admin@test.pl, user@test.pl");
    }
}
