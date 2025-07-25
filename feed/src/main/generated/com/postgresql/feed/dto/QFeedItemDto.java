package com.postgresql.feed.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.postgresql.feed.dto.QFeedItemDto is a Querydsl Projection type for FeedItemDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QFeedItemDto extends ConstructorExpression<FeedItemDto> {

    private static final long serialVersionUID = -973637060L;

    public QFeedItemDto(com.querydsl.core.types.Expression<Long> feedItemId, com.querydsl.core.types.Expression<UserDto> user, com.querydsl.core.types.Expression<PageDto> page, com.querydsl.core.types.Expression<? extends java.util.List<HighlightDto>> highlights, com.querydsl.core.types.Expression<Integer> highlightCount, com.querydsl.core.types.Expression<java.time.LocalDateTime> firstHighlightAt) {
        super(FeedItemDto.class, new Class<?>[]{long.class, UserDto.class, PageDto.class, java.util.List.class, int.class, java.time.LocalDateTime.class}, feedItemId, user, page, highlights, highlightCount, firstHighlightAt);
    }

}

