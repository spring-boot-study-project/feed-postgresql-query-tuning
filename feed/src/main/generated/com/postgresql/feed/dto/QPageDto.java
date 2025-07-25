package com.postgresql.feed.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.postgresql.feed.dto.QPageDto is a Querydsl Projection type for PageDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QPageDto extends ConstructorExpression<PageDto> {

    private static final long serialVersionUID = -1413352514L;

    public QPageDto(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> url, com.querydsl.core.types.Expression<String> title, com.querydsl.core.types.Expression<String> domain) {
        super(PageDto.class, new Class<?>[]{long.class, String.class, String.class, String.class}, id, url, title, domain);
    }

}

