package com.postgresql.feed.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

public record FeedItemDto(
    Long feedItemId,
    UserDto user,
    PageDto page,
    List<HighlightDto> highlights,
    Integer highlightCount,
    LocalDateTime firstHighlightAt
) {

    @QueryProjection
    public FeedItemDto(
        Long feedItemId, UserDto user, PageDto page, List<HighlightDto> highlights,
        Integer highlightCount, LocalDateTime firstHighlightAt
    ) {
        this.feedItemId = feedItemId;
        this.user = user;
        this.page = page;
        this.highlights = highlights;
        this.highlightCount = highlightCount;
        this.firstHighlightAt = firstHighlightAt;
    }
}
