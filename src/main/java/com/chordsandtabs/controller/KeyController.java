package com.chordsandtabs.controller;

import com.chordsandtabs.dto.key.KeyDto;
import com.chordsandtabs.repository.KeyRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/keys")
public class KeyController {

    private final KeyRepository keyRepository;

    public KeyController(KeyRepository keyRepository) {
        this.keyRepository = keyRepository;
    }

    @GetMapping
    List<KeyDto> getAll() {
        var keys = new ArrayList<>(keyRepository.findAllByOrderByNameAsc());
        return keys.stream()
                .map(k -> new KeyDto(k.getKeyId(), k.getName(), k.getMode().name()))
                .toList();
    }
}
