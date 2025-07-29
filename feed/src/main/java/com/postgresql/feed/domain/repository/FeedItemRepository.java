package com.postgresql.feed.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.postgresql.feed.domain.FeedItem;
import com.postgresql.feed.dto.HighlightDto;

/**
 * FeedItem에 있는 하이라이트를 따로 조회
 */
public interface FeedItemRepository extends JpaRepository<FeedItem, Long>, FeedItemRepositoryCustom {
    
    /**
     * 특정 사용자와 페이지들의 하이라이트 조회 (페이지당 최대 3개)
     * PostgreSQL Window Function을 사용하여 DB 레벨에서 제한 여기서 사용하는 파티셔닝은 물리적으로 분리하는 파티셔닝과 다르다.
     * 최종적으로 정렬 보장을 위해서 ORDER BY 적용 -> 같은 페이지의 하이라이트를 묶어서 반환
     * 
     * @param userId  사용자 ID
     * @param pageIds 페이지 ID 목록
     * @return 하이라이트 DTO 목록 (페이지당 최대 3개)
     */
    @Query(value = """
            WITH ranked_highlights AS (
                SELECT h.id,
                       h.text,
                       h.color,
                       h.created_at,
                       h.page_id,
                       ROW_NUMBER() OVER (PARTITION BY h.page_id ORDER BY h.created_at DESC) as rn
                FROM highlights h
                WHERE h.page_id IN (:pageIds)
            )
            SELECT id,
                   text,
                   color,
                   created_at as createdAt,
                   page_id as feedItemId
            FROM ranked_highlights
            WHERE rn <= 3
            ORDER BY page_id, rn
            """, nativeQuery = true)
    List<HighlightDto> findHighlightsByPages(@Param("pageIds") List<Long> pageIds);
}
