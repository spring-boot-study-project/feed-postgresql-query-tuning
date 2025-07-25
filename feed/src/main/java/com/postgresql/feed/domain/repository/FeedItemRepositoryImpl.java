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
        BooleanExpression existHighlight = feedItem.highlightCount.gt(0);

        List<Long> feedItemIds = queryFactory // 이 시점에서 조건에 맞는 데이터 조회
                .select(feedItem.id)
                .from(feedItem)
                .where(
                    createVisibilityCondition(userId)
                        .and(createCursorCondition(cursorFirstHighlightAt, cursorId))
                        .and(existHighlight))
                .orderBy(
                    feedItem.firstHighlightAt.desc(), // 과제 조건에 따라 최신순으로 내림차순 정렬
                    feedItem.id.desc()
                )
                .limit(limit)
                .fetch();

        if (feedItemIds.isEmpty()) { // 없어면 데이터 빈 데이터로 변경
            return Collections.emptyList();
        }

        return queryFactory
                .select(
                    new QFeedItemDto(
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
                        Expressions.constant(Collections.emptyList()), // 이시점에서는 하이라이트 정보가 없으므로 null
                        feedItem.highlightCount,
                        feedItem.firstHighlightAt)
                )
                .from(feedItem)
                .join(feedItem.user, user)
                .join(feedItem.page, page)
                .where(feedItem.id.in(feedItemIds))
                .orderBy(
                        feedItem.firstHighlightAt.desc(), // 과제 조건에 따라 최신순으로 내림차순 정렬
                        feedItem.id.desc())
                .fetch();
    }

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
