package com.postgresql.feed.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HighlightDto {
    private Long id;
    private String text;
    private String color;
    private LocalDateTime createdAt;
    private Long feedItemId; // 피드 아이템 ID 추가

    @QueryProjection
    public HighlightDto(Long id, String text, String color, Timestamp createdAt, Long feedItemId) {
        this.id = id;
        this.text = text;
        this.color = color;
        this.createdAt = createdAt.toLocalDateTime();
        this.feedItemId = feedItemId;
    }
}
