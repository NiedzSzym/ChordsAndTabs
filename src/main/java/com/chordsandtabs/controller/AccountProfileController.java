package com.chordsandtabs.controller;

import com.chordsandtabs.dto.profile.AccountProfileDto;
import com.chordsandtabs.dto.profile.AccountProfileUpdateRequest;
import com.chordsandtabs.model.Account;
import com.chordsandtabs.model.AccountProfile;
import com.chordsandtabs.repository.AccountProfileRepository;
import com.chordsandtabs.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/profile")
public class AccountProfileController {

    private final AccountProfileRepository accountProfileRepository;
    private final CurrentUserService currentUserService;

    public AccountProfileController(AccountProfileRepository accountProfileRepository,
                                    CurrentUserService currentUserService) {
        this.accountProfileRepository = accountProfileRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    ResponseEntity<AccountProfileDto> getMyProfile() {
        Account currentUser = currentUserService.getCurrentUser();
        return accountProfileRepository.findById(currentUser.getAccountId())
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    ResponseEntity<AccountProfileDto> getProfile(@PathVariable Long id) {
        return accountProfileRepository.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    ResponseEntity<Void> createProfile(@RequestBody @Valid AccountProfileUpdateRequest req) {
        Account currentUser = currentUserService.getCurrentUser();
        if (accountProfileRepository.findById(currentUser.getAccountId()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        AccountProfile profile = new AccountProfile();
        profile.setAccount(currentUser);
        profile.setNickname(req.nickname());
        profile.setBio(req.bio());
        accountProfileRepository.save(profile);
        return ResponseEntity.created(URI.create("/api/profile/" + profile.getAccountId())).build();
    }

    @PutMapping
    ResponseEntity<Void> updateProfile(@RequestBody @Valid AccountProfileUpdateRequest req) {
        Account currentUser = currentUserService.getCurrentUser();
        AccountProfile profile = accountProfileRepository.findById(currentUser.getAccountId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setNickname(req.nickname());
        profile.setBio(req.bio());
        profile.setUpdatedAt(OffsetDateTime.now());
        accountProfileRepository.save(profile);
        return ResponseEntity.noContent().build();
    }

    private AccountProfileDto toDto(AccountProfile profile) {
        return new AccountProfileDto(
                profile.getAccountId(),
                profile.getNickname(),
                profile.getBio(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
