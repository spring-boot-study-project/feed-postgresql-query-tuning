package com.postgresql.feed.domain;

import com.postgresql.feed.domain.common.BaseTimeEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "highlights")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Highlight extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

    @Column(nullable = false, length = 500)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HighlightColor color = HighlightColor.YELLOW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HighlightVisibility visibility = HighlightVisibility.PUBLIC;

    @Column(name = "start_offset", nullable = false)
    private Integer startOffset;

    @Column(name = "end_offset", nullable = false)
    private Integer endOffset;

    @Column(name = "context_before", length = 500)
    private String contextBefore;

    @Column(name = "context_after", length = 500)
    private String contextAfter;

    // 생성자
    @Builder
    public Highlight(User user, Page page, String text, Integer startOffset, Integer endOffset) {
        this.user = user;
        this.page = page;
        this.text = text;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public enum HighlightColor {
        YELLOW, GREEN, BLUE, PINK, PURPLE, ORANGE, RED
    }

    public enum HighlightVisibility {
        PUBLIC, PRIVATE, MENTIONED
    }
}
