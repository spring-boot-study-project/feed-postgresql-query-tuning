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
    @Index(name = "idx_feed_user_first_highlight", columnList = "user_id, first_highlight_at DESC"), //visibility = 'PRIVATE' AND user_id = :userId 여기서 사용
    @Index(name = "idx_feed_page", columnList = "page_id"), //highlight.page.eq(feedItem.page) 여기서 사용
    @Index(name = "idx_feed_visibility", columnList = "visibility"), //feedItem.visibility.eq(FeedVisibility.PUBLIC) 여기서 사용
    @Index(name = "idx_feed_visibility_user", columnList = "visibility, user_id"), //feedItem.visibility.eq(FeedVisibility.PRIVATE).and(feedItem.user.id.eq(userId)) 2, 3번째 조건에 대해서 인덱스
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
