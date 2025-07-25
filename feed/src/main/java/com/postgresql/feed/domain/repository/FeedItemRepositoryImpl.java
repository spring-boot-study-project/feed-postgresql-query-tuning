package com.postgresql.feed.domain.repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.postgresql.feed.domain.FeedItem.FeedVisibility;
import com.postgresql.feed.dto.FeedItemDto;
import com.postgresql.feed.dto.QFeedItemDto;
import com.postgresql.feed.dto.QPageDto;
import com.postgresql.feed.dto.QUserDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import static com.postgresql.feed.domain.QFeedItem.feedItem;
import static com.postgresql.feed.domain.QPage.page;
import static com.postgresql.feed.domain.QUser.user;
import static com.postgresql.feed.domain.QHighlight.highlight;
import static com.postgresql.feed.domain.QMention.mention;

@Repository
@RequiredArgsConstructor
public class FeedItemRepositoryImpl implements FeedItemRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<FeedItemDto> findFeedItemsWithUserAndPage(Long userId, int limit, LocalDateTime cursorFirstHighlightAt, Long cursorId) {
        BooleanExpression existHighlight = feedItem.highlightCount.gt(0); // 하이라이트가 있는지 없는지 확인

        // 성능 최적화 분리했던 쿼리를 하나로 합침침
        return queryFactory
                .select(new QFeedItemDto(
                        feedItem.id,
                        new QUserDto(
                            user.id,
                            user.username,
                            user.nickName),
                        new QPageDto(
                            page.id,
                            page.url,
                            page.title,
                            page.domain),
                        Expressions.constant(Collections.emptyList()), // 하이라이트는 별도 조회
                        feedItem.highlightCount,
                        feedItem.firstHighlightAt)
                )
                .from(feedItem)
                .join(feedItem.user, user)
                .join(feedItem.page, page)
                .where(
                    createVisibilityCondition(userId) // 가시성 모두 확인
                        .and(createCursorCondition(cursorFirstHighlightAt, cursorId)) // 커서 조건 확인
                        .and(existHighlight)) // 하이라이트가 있는지 없는지 확인
                .orderBy(
                    feedItem.firstHighlightAt.desc(),
                    feedItem.id.desc())
                .limit(limit)
                .fetch();
    }
    
    // @Override
    // public List<FeedItemDto> findPublicFeedsOnly(int limit) {
    //     BooleanExpression existHighlight = feedItem.highlightCount.gt(0);
    //     BooleanExpression publicCondition = feedItem.visibility.eq(FeedVisibility.PUBLIC);

    //     // 🚀 성능 최적화: 단일 쿼리로 통합
    //     return queryFactory
    //             .select(new QFeedItemDto(
    //                     feedItem.id,
    //                     new QUserDto(
    //                         user.id,
    //                         user.username,
    //                         user.nickName),
    //                     new QPageDto(
    //                         page.id,
    //                         page.url,
    //                         page.title,
    //                         page.domain),
    //                     Expressions.constant(Collections.emptyList()),
    //                     feedItem.highlightCount,
    //                     feedItem.firstHighlightAt)
    //             )
    //             .from(feedItem)
    //             .join(feedItem.user, user)  // DTO 프로젝션에서는 일반 JOIN으로 충분
    //             .join(feedItem.page, page)  // DTO 프로젝션에서는 일반 JOIN으로 충분
    //             .where(publicCondition.and(existHighlight))
    //             .orderBy(
    //                 feedItem.firstHighlightAt.desc(),
    //                 feedItem.id.desc()
    //             )
    //             .limit(limit)
    //             .fetch();
    // }
    
    // @Override
    // public List<FeedItemDto> findPrivateAndMentionedFeeds(Long userId, int limit) {
    //     BooleanExpression existHighlight = feedItem.highlightCount.gt(0);
    //     BooleanExpression privateCondition = feedItem.visibility.eq(FeedVisibility.PRIVATE).and(feedItem.user.id.eq(userId));
        
    //     // mention 조건을 EXISTS 서브쿼리로 변경하여 조인 없이 처리
    //     BooleanExpression mentionCondition = feedItem.visibility.eq(FeedVisibility.MENTIONED)
    //             .and(JPAExpressions.selectOne()
    //                     .from(mention)
    //                     .join(mention.highlight, highlight)
    //                     .where(
    //                             highlight.page.eq(feedItem.page)
    //                                     .and(mention.mentionedUser.id.eq(userId)))
    //                     .exists());

    //     // 🚀 성능 최적화: 단일 쿼리로 통합
    //     return queryFactory
    //             .select(new QFeedItemDto(
    //                     feedItem.id,
    //                     new QUserDto(
    //                         user.id,
    //                         user.username,
    //                         user.nickName),
    //                     new QPageDto(
    //                         page.id,
    //                         page.url,
    //                         page.title,
    //                         page.domain),
    //                     Expressions.constant(Collections.emptyList()),
    //                     feedItem.highlightCount,
    //                     feedItem.firstHighlightAt)
    //             )
    //             .from(feedItem)
    //             .join(feedItem.user, user)  // DTO 프로젝션에서는 일반 JOIN으로 충분
    //             .join(feedItem.page, page)  // DTO 프로젝션에서는 일반 JOIN으로 충분
    //             .where(
    //                 privateCondition.or(mentionCondition)
    //                     .and(existHighlight))
    //             .orderBy(
    //                 feedItem.firstHighlightAt.desc(),
    //                 feedItem.id.desc())
    //             .limit(limit)
    //             .fetch();
    // }

    private BooleanExpression createVisibilityCondition(Long userId) {
        if(userId == null) {
            return feedItem.visibility.eq(FeedVisibility.PUBLIC);
        }

        BooleanExpression publicCondition = feedItem.visibility.eq(FeedVisibility.PUBLIC);
        BooleanExpression privateCondition = feedItem.visibility.eq(FeedVisibility.PRIVATE).and(feedItem.user.id.eq(userId));

        // mention 조건을 EXISTS 서브쿼리로 변경하여 조인 없이 처리
        // mention -> highlight -> page -> feedItem 경로로 연결
        BooleanExpression mentionCondition = feedItem.visibility.eq(FeedVisibility.MENTIONED)
                .and(JPAExpressions.selectOne()
                        .from(mention)
                        .join(mention.highlight, highlight)
                        .where(
                                highlight.page.eq(feedItem.page)
                                        .and(mention.mentionedUser.id.eq(userId)))
                        .exists());

        return publicCondition // public인 경우 데이터 가져오기
                .or(privateCondition) // private인 경우 본인 데이터 가져오기
                .or(mentionCondition);
    }

    private BooleanExpression createCursorCondition(LocalDateTime cursorFirstHighlightAt, Long cursorId) {
        if(cursorFirstHighlightAt == null || cursorId == null) {
                return null;
        }

        // 여기서 조건은 ORDER BY firstHighlightAt.desc(), id.desc()에 맞는 조건이어야 되는데..
        return feedItem.firstHighlightAt.lt(cursorFirstHighlightAt) // cursorFirstHighlightAt 이 값보다 작거나
            .or(
                (feedItem.firstHighlightAt.eq(cursorFirstHighlightAt).and(feedItem.id.lt(cursorId)))
            ); // cursorFirstHighlightAt 이 값이 같고 커서 id와 같은 경우
    }
}
