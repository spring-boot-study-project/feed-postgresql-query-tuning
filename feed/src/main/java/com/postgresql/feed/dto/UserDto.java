package com.postgresql.feed.dto;

import com.querydsl.core.annotations.QueryProjection;

public record UserDto(
    Long id,
    String username,
    String nickName
) {

    @QueryProjection
    public UserDto(Long id, String username, String nickName) {
        this.id = id;
        this.username = username;
        this.nickName = nickName;
    }
}
