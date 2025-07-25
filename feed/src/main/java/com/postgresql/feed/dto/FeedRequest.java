package com.postgresql.feed.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FeedRequest(
    @NotNull(message = "사용자 ID는 필수입니다")
    Long userId,
        
    @Min(value = 1, message = "조회할 개수는 1 이상이어야 합니다")
    @Max(value = 100, message = "조회할 개수는 100 이하여야 합니다")
    Integer size,
    
    String cursor
) {
    public FeedRequest {
        if (size == null) {
            size = 20;
        }
    }
}
