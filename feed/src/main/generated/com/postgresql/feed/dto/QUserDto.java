package com.postgresql.feed.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.postgresql.feed.dto.QUserDto is a Querydsl Projection type for UserDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QUserDto extends ConstructorExpression<UserDto> {

    private static final long serialVersionUID = -756936446L;

    public QUserDto(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> username, com.querydsl.core.types.Expression<String> nickName) {
        super(UserDto.class, new Class<?>[]{long.class, String.class, String.class}, id, username, nickName);
    }

}

