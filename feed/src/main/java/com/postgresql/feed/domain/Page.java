package com.postgresql.feed.domain;

import com.postgresql.feed.domain.common.BaseTimeEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "pages")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Page extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String url;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, length = 200)
    private String domain;

    @Column(name = "favicon_url", length = 500)
    private String faviconUrl;

    @Column(name = "og_image_url", length = 500)
    private String ogImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PageStatus status = PageStatus.ACTIVE;

    // 생성자
    @Builder
    public Page(String url, String title, String domain) {
        this.url = url;
        this.title = title;
        this.domain = domain;
    }

    public enum PageStatus {
        ACTIVE, INACTIVE, BLOCKED
    }
}
