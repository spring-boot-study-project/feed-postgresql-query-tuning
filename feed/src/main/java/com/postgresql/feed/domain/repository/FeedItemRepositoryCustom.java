package com.postgresql.feed.domain.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.postgresql.feed.dto.FeedItemDto;

public interface FeedItemRepositoryCustom {
    
    /**
     * 사용자의 피드 아이템 조회 (기본 정보만, 하이라이트 제외)
     * QueryDSL을 사용하여 DB 레벨에서 페이징 적용
     * 
     * @param userId 사용자 ID
     * @param limit 조회할 개수
     * @param offset 시작 위치
     * @return 피드 아이템 DTO 목록
     */
    List<FeedItemDto> findFeedItemsWithUserAndPage(Long userId, int limit, LocalDateTime cursorFirstHighlightAt, Long cursorId);
}

