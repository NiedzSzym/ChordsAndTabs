package com.chordsandtabs.controller;

import com.chordsandtabs.dto.key.KeyDto;
import com.chordsandtabs.model.Key;
import com.chordsandtabs.model.Mode;
import com.chordsandtabs.repository.KeyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeyControllerTest {

    @Mock
    private KeyRepository keyRepository;

    @InjectMocks
    private KeyController controller;

    private Key createKey(Long id, String name, String mode) {
        Key key = new Key();
        key.setKeyId(id);
        key.setName(name);
        key.setMode(Mode.valueOf(mode));
        return key;
    }

    @Test
    void getAll_shouldReturnAllKeys() {
        Key cMajor = createKey(1L, "C", "MAJOR");
        Key aMinor = createKey(2L, "Am", "MINOR");

        when(keyRepository.findAllByOrderByNameAsc()).thenReturn(List.of(cMajor, aMinor));

        List<KeyDto> result = controller.getAll();

        assertEquals(2, result.size());
        assertEquals("C", result.get(0).name());
        assertEquals("MAJOR", result.get(0).mode());
        assertEquals("Am", result.get(1).name());
        assertEquals("MINOR", result.get(1).mode());
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoKeys() {
        when(keyRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

        List<KeyDto> result = controller.getAll();

        assertEquals(0, result.size());
    }
}
