package com.chordsandtabs.controller;

import com.chordsandtabs.dto.security.AuthResponse;
import com.chordsandtabs.dto.security.LoginRequest;
import com.chordsandtabs.dto.security.RegisterRequest;
import com.chordsandtabs.model.Account;
import com.chordsandtabs.model.Role;
import com.chordsandtabs.repository.AccountRepository;
import com.chordsandtabs.repository.RoleRepository;
import com.chordsandtabs.security.JwtUtil;
import com.chordsandtabs.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthController controller;

    private Role createRole(String name) {
        Role role = new Role();
        role.setRoleId(1L);
        role.setName(name);
        return role;
    }

    private Account createAccount(String email, Role role, OffsetDateTime emailVerifiedAt) {
        Account account = new Account();
        account.setAccountId(1L);
        account.setEmail(email);
        account.setRole(role);
        account.setEmailVerifiedAt(emailVerifiedAt);
        return account;
    }

    @Test
    void register_shouldCreateAccountAndSendVerification() {
        RegisterRequest request = new RegisterRequest("user@test.pl", "password123".toCharArray());
        Role userRole = createRole("ROLE_USER");
        String hashed = "hashedPassword";

        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn(hashed);
        when(jwtUtil.generateVerificationToken("user@test.pl")).thenReturn("verification-token");
        doNothing().when(emailService).sendVerificationEmail("user@test.pl", "verification-token");
        when(accountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<String> response = controller.register(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered. Please check your email to verify.", response.getBody());
        verify(accountRepository).save(any());
        verify(emailService).sendVerificationEmail("user@test.pl", "verification-token");
    }

    @Test
    void register_shouldThrow_whenRoleNotFound() {
        RegisterRequest request = new RegisterRequest("user@test.pl", "password123".toCharArray());

        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> controller.register(request));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void verify_shouldVerifyEmail() {
        String token = "valid-token";
        String email = "user@test.pl";
        Account account = createAccount(email, createRole("ROLE_USER"), null);

        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(accountRepository.findAccountByEmail(email)).thenReturn(Optional.of(account));

        ResponseEntity<String> response = controller.verify(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Email verified successfully", response.getBody());
        assertNotNull(account.getEmailVerifiedAt());
        verify(accountRepository).save(account);
    }

    @Test
    void verify_shouldReturnAlreadyVerified_whenAlreadyVerified() {
        String token = "valid-token";
        String email = "user@test.pl";
        Account account = createAccount(email, createRole("ROLE_USER"), OffsetDateTime.now());

        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(accountRepository.findAccountByEmail(email)).thenReturn(Optional.of(account));

        ResponseEntity<String> response = controller.verify(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Email already verified", response.getBody());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void verify_shouldThrow_whenUserNotFound() {
        String token = "invalid-token";

        when(jwtUtil.extractEmail(token)).thenReturn("unknown@test.pl");
        when(accountRepository.findAccountByEmail("unknown@test.pl")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> controller.verify(token));
    }

    @Test
    void login_shouldReturnToken_whenVerified() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.pl");
        request.setPasswordFromString("password123");
        Role role = createRole("ROLE_USER");
        Account account = createAccount("user@test.pl", role, OffsetDateTime.now());

        when(accountRepository.findAccountByEmail("user@test.pl")).thenReturn(Optional.of(account));
        when(jwtUtil.generateToken("user@test.pl", "ROLE_USER")).thenReturn("jwt-token");

        ResponseEntity<AuthResponse> response = controller.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-token", response.getBody().token());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_shouldReturn403_whenNotVerified() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.pl");
        request.setPasswordFromString("password123");
        Role role = createRole("ROLE_USER");
        Account account = createAccount("user@test.pl", role, null);

        when(accountRepository.findAccountByEmail("user@test.pl")).thenReturn(Optional.of(account));
        when(jwtUtil.generateToken("user@test.pl", "ROLE_USER")).thenReturn("token");

        ResponseEntity<AuthResponse> response = controller.login(request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Please verify your email first", response.getBody().token());
    }
}
