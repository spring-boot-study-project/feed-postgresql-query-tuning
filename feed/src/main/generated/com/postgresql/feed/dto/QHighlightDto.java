package com.postgresql.feed.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.postgresql.feed.dto.QHighlightDto is a Querydsl Projection type for HighlightDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QHighlightDto extends ConstructorExpression<HighlightDto> {

    private static final long serialVersionUID = -2035322275L;

    public QHighlightDto(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> text, com.querydsl.core.types.Expression<String> color, com.querydsl.core.types.Expression<? extends java.sql.Timestamp> createdAt, com.querydsl.core.types.Expression<Long> feedItemId) {
        super(HighlightDto.class, new Class<?>[]{long.class, String.class, String.class, java.sql.Timestamp.class, long.class}, id, text, color, createdAt, feedItemId);
    }

}

