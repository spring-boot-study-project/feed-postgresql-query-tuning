package com.postgresql.feed.domain;

import com.postgresql.feed.domain.common.BaseTimeEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mentions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mention extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "highlight_id", nullable = false)
    private Highlight highlight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentioned_user_id", nullable = false)
    private User mentionedUser;

    // 생성자
    @Builder
    public Mention(Highlight highlight, User mentionedUser) {
        this.highlight = highlight;
        this.mentionedUser = mentionedUser;
    }
}
