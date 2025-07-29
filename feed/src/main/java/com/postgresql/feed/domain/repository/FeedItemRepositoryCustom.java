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
     * @param cursorFirstHighlightAt 커서 시간
     * @param cursorId 커서 ID
     * @return 피드 아이템 DTO 목록
     */
    List<FeedItemDto> findFeedItemsWithUserAndPage(Long userId, int limit, LocalDateTime cursorFirstHighlightAt, Long cursorId);
    
    // /**
    //  * PUBLIC 피드만 조회 (전역 캐시용)
    //  * 모든 사용자가 볼 수 있는 PUBLIC 피드만 조회
    //  * 
    //  * @param limit 조회할 개수
    //  * @return PUBLIC 피드 아이템 DTO 목록
    //  */
    // List<FeedItemDto> findPublicFeedsOnly(int limit);
    
    // /**
    //  * 사용자별 PRIVATE 및 MENTIONED 피드만 조회
    //  * 해당 사용자만 볼 수 있는 PRIVATE 피드와 멘션된 MENTIONED 피드만 조회
    //  * 
    //  * @param userId 사용자 ID
    //  * @param limit 조회할 개수
    //  * @return PRIVATE/MENTIONED 피드 아이템 DTO 목록
    //  */
    // List<FeedItemDto> findPrivateAndMentionedFeeds(Long userId, int limit);
}
