package com.chordsandtabs.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class NotationTypeControllerTest {

    private final NotationTypeController controller = new NotationTypeController();

    @Test
    void getAll_shouldReturnAllNotationTypes() {
        List<String> result = controller.getAll();

        assertEquals(List.of("CHORDS", "TABS"), result);
    }
}
