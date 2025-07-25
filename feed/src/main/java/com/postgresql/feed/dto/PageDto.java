package com.postgresql.feed.dto;

import com.querydsl.core.annotations.QueryProjection;

public record PageDto(
    Long id,
    String url,
    String title,
    String domain
) {
    @QueryProjection
    public PageDto(Long id, String url, String title, String domain) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.domain = domain;
    }
}
