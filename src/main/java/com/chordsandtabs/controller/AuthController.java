package com.chordsandtabs.controller;

import com.chordsandtabs.dto.AuthResponse;
import com.chordsandtabs.dto.LoginRequest;
import com.chordsandtabs.dto.RegisterRequest;
import com.chordsandtabs.model.Account;
import com.chordsandtabs.model.Role;
import com.chordsandtabs.repository.AccountRepository;
import com.chordsandtabs.repository.RoleRepository;
import com.chordsandtabs.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(PasswordEncoder passwordEncoder,
                          AccountRepository accountRepository,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          RoleRepository roleRepository) {
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        Account account = new Account();

        account.setEmail(request.email());

        String rawPassword = String.valueOf(request.password());
        String hashedPassword = passwordEncoder.encode(rawPassword);
        Arrays.fill(request.password(), '\0');
        Objects.requireNonNull(hashedPassword, "Encoder returned null");
        account.setPassword(hashedPassword.toCharArray());

        Role accountRole = roleRepository.findByName("ROLE_USER")
                        .orElseThrow(() -> new RuntimeException("ROLE_USER doesn't exist in database"));
        account.setRole(accountRole);

        accountRepository.save(account);

        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), String.valueOf(request.password()))
        );
        Account account = accountRepository.findAccountByEmail(request.email()).orElseThrow();
        String token = jwtUtil.generateToken(account.getEmail(), account.getRole().getName());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
