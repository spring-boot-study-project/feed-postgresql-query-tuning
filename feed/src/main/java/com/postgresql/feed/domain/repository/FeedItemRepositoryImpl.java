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
                        Expressions.constant(Collections.emptyList()), // 이시점에서는 하이라이트 정보가 없으므로 빈 리스트
                        feedItem.highlightCount,
                        feedItem.firstHighlightAt)
                )
                .from(feedItem) // feedItem.id, feedItem.page_id, feedItem.user_id 이 값을 가지고 inner 조인
                .join(feedItem.user, user) // inner 조인 -> DTO 프로젝션 조인에서는 즉시/지연 로딩 개념이 없다 .fetchJoin() 필요 x
                .join(feedItem.page, page) // inner 조인 -> DTO 프로젝션 조인에서는 즉시/지연 로딩 개념이 없다 .fetchJoin() 필요 x
                .leftJoin(highlight).on(highlight.page.id.eq(page.id).and(highlight.user.id.eq(feedItem.user.id)))
                .leftJoin(mention).on(mention.highlight.id.eq(highlight.id))
                .where(
                    createVisibilityCondition(userId)
                    .and(createCursorCondition(cursorFirstHighlightAt, cursorId))
                )
                .groupBy(
                        feedItem.id,
                        user.id,
                        user.username,
                        page.id,
                        page.url,
                        page.title,
                        page.domain,
                        feedItem.highlightCount,
                        feedItem.firstHighlightAt
                ) // group by를 통해서 행들을 하나로 합친다.
                .orderBy(
                        feedItem.firstHighlightAt.desc(), //과제 조건에 따라 최신순으로 내림차순 정렬
                        feedItem.id.desc() // 앞서 나온 값이 동일한 값이 존재할 수 있기 때문에 이 경우 id 값으로 내림차순으로 정렬
                )
                .limit(limit)
                .fetch();
    }

    private BooleanExpression createVisibilityCondition(Long userId) {
        if(userId == null) {
            return feedItem.visibility.eq(FeedVisibility.PUBLIC);
        }

        BooleanExpression publicCondition = feedItem.visibility.eq(FeedVisibility.PUBLIC);
        BooleanExpression privateCondition = feedItem.visibility.eq(FeedVisibility.PRIVATE).and(feedItem.user.id.eq(userId));
        BooleanExpression mentionCondition = feedItem.visibility.eq(FeedVisibility.MENTIONED).and(mention.mentionedUser.id.eq(userId));
        
        return publicCondition // public인 경우 데이터 가져오기
                .or(privateCondition) // private인 경우 본인 데이터 가져오기
                .or(mentionCondition); // mention인 경우 데이터 가져오기
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
