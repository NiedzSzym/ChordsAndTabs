package com.chordsandtabs.dto.song;

import java.util.List;

public record SongsListDto(Long id, String name, List<String> artistNames) {
}
