package com.postgresql.feed.domain;

import java.time.LocalDateTime;

import com.postgresql.feed.domain.common.BaseTimeEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feed_items", indexes = {
    @Index(name = "idx_feed_public_optimized", columnList = "visibility, highlight_count, first_highlight_at DESC, id DESC"), // PUBLIC 피드 전용: visibility='PUBLIC' ->  highlight_count > 0 -> ORDER BY 최적화 2개 컬럼에 대해서 복합 인덱스 적용 여기서 커서에 대한 조건이 들어가는데.. 인덱스 타는걸 방해하려나..
    @Index(name = "idx_feed_private_optimized", columnList = "visibility, user_id, highlight_count, first_highlight_at DESC, id DESC"), // PRIVATE 피드 전용: visibility='PRIVATE' -> user_id=? -> highlight_count > 0 -> ORDER BY 최적화 2개 컬럼에 대해서 복합 인덱스 적용 -> 여기도 마찬가지
    @Index(name = "idx_feed_mentioned_optimized", columnList = "visibility, highlight_count, first_highlight_at DESC, id DESC"), // MENTIONED 피드 전용: visibility='MENTIONED' AND highlight_count>0 + ORDER BY 최적화
    @Index(name = "idx_feed_page", columnList = "page_id"), // EXISTS 서브쿼리에서 highlight.page.eq(feedItem.page) 조건 최적화
    @Index(name = "idx_feed_page_visibility_highlight", columnList = "visibility, page_id, highlight_count, first_highlight_at DESC, id DESC"), // MENTIONED 피드의 복합 조건 최적화 (visibility=MENTIONED -> page_id로 강력한 필터링 -> highlight_count)
    @Index(name = "idx_feed_highlight_count_first", columnList = "highlight_count, first_highlight_at DESC, id DESC") // 모든 쿼리의 highlight_count > 0 필터링 + ORDER BY 정렬 최적화 (다른 인덱스 사용 불가 시 fallback)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

    @Column(name = "first_highlight_at", nullable = false)
    private LocalDateTime firstHighlightAt;

    @Column(name = "last_highlight_at", nullable = false)
    private LocalDateTime lastHighlightAt;

    @Column(name = "highlight_count", nullable = false)
    private Integer highlightCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedVisibility visibility = FeedVisibility.PUBLIC;

    // 생성자
    @Builder
    public FeedItem(User user, Page page, LocalDateTime firstHighlightAt) {
        this.user = user;
        this.page = page;
        this.firstHighlightAt = firstHighlightAt;
        this.lastHighlightAt = firstHighlightAt;
        this.highlightCount = 1;
    }

    public enum FeedVisibility {
        PUBLIC, PRIVATE, MENTIONED
    }
}
