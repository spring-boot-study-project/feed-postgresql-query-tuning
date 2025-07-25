package com.postgresql.feed.dto;

import java.util.List;

public record FeedResponse(
    List<FeedItemDto> data,
    PaginationDto pagination
) {
    public static FeedResponse of(List<FeedItemDto> data, PaginationDto pagination) {
        return new FeedResponse(data, pagination);
    }
}
