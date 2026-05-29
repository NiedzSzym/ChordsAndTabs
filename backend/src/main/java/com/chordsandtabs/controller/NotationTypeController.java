package com.chordsandtabs.controller;

import com.chordsandtabs.model.NotationType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/notation-types")
public class NotationTypeController {

    @GetMapping
    public List<String> getAll() {
        return Arrays.stream(NotationType.values())
                .map(Enum::name)
                .toList();
    }
}
