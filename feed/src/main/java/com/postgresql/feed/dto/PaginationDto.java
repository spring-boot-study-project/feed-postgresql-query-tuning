package com.postgresql.feed.dto;

public record PaginationDto(
    String nextCursor,
    boolean hasNext,
    int limit
) {
    public static PaginationDto of(String nextCursor, boolean hasNext, int limit) {
        return new PaginationDto(nextCursor, hasNext, limit);
    }
}
