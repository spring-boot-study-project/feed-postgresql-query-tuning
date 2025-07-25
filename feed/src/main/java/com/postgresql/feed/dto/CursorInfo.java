package com.postgresql.feed.dto;

import java.time.LocalDateTime;

public record CursorInfo(
    LocalDateTime firstHighlightAt, 
    Long id
) {
}
