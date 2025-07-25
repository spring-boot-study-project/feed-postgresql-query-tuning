package com.postgresql.feed.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.postgresql.feed.domain.repository.FeedItemRepository;
import com.postgresql.feed.dto.CursorInfo;
import com.postgresql.feed.dto.FeedItemDto;
import com.postgresql.feed.dto.FeedResponse;
import com.postgresql.feed.dto.HighlightDto;
import com.postgresql.feed.dto.PaginationDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FeedService {
    private final FeedItemRepository feedItemRepository;

    public FeedResponse getUserFeeds(Long userId, int size, String cursor) {
        // 다음 페이지 존재 여부 확인을 위해 +1개 조회
        int queryLimit = size + 1;

        // 커서 파싱
        CursorInfo cursorInfo = parseCursor(cursor);

        // 1. 피드 아이템 기본 정보 조회 PUBLIC, PRIVATE (DB 레벨에서 커서 기반 페이징 적용 -> 단 여기서 하이라이트 정보 가져오지 않음)
        List<FeedItemDto> feedItems = feedItemRepository.findFeedItemsWithUserAndPage(userId, queryLimit, cursorInfo.firstHighlightAt(), cursorInfo.id());
        
        // 만약 다음 페이지가 있다면 true로 바꾸고 size으로 셋팅
        boolean hasNext = feedItems.size() > size;
        if (hasNext) {
            feedItems = feedItems.subList(0, size);
        }

        // 2. 하이라이트 정보 조회 및 조합
        List<FeedItemDto> feedItemsWithHighlights = addHighlightsToFeedItems(userId, feedItems);

        // cursor 생성 (마지막 아이템의 정보로)
        String nextCursor = null;
        if (hasNext && !feedItemsWithHighlights.isEmpty()) {
            FeedItemDto lastItem = feedItemsWithHighlights.get(feedItemsWithHighlights.size() - 1);
            nextCursor = generateCursor(lastItem); // 커서 생성
        }

        PaginationDto pagination = PaginationDto.of(nextCursor, hasNext, size);

        return FeedResponse.of(feedItemsWithHighlights, pagination);
    }

    /**
     * 피드 아이템에 하이라이트 정보를 추가
     * 
     * @param userId    사용자 ID
     * @param feedItems 피드 아이템 목록
     * @return 하이라이트가 포함된 피드 아이템 목록
     */
    private List<FeedItemDto> addHighlightsToFeedItems(Long userId, List<FeedItemDto> feedItems) {
        if (feedItems.isEmpty()) {
            return feedItems;
        }

        // 페이지 ID 목록 추출 (이미 Repository에서 GROUP BY로 중복 제거됨)
        List<Long> pageIds = feedItems.stream()
                .map(item -> item.page().id())
                .collect(Collectors.toList());
        
        List<HighlightDto> highlights = feedItemRepository.findHighlightsByPages(pageIds);


        // 순차적 처리 방식: highlights가 pageIds 배열 순서(feedItems의 first_highlight_at DESC 순서)로
        // 정렬되어 있으므로 효율적
        Map<Long, List<HighlightDto>> highlightMap = new HashMap<>();
        for (HighlightDto highlight : highlights) {
            highlightMap.computeIfAbsent(highlight.getFeedItemId(), k -> new ArrayList<>()).add(highlight);
        }

        // 피드 아이템과 하이라이트 조합
        return feedItems.stream()
                .map(item -> new FeedItemDto(
                        item.feedItemId(),
                        item.user(),
                        item.page(),
                        highlightMap.getOrDefault(item.page().id(), Collections.emptyList()), // null 안정성 강화
                        item.highlightCount(),
                        item.firstHighlightAt()))
                .collect(Collectors.toList());
    }

    /**
     * cursor 생성 (Base64 인코딩된 timestamp_id 형태)
     */
    private String generateCursor(FeedItemDto item) {
        String cursorData = item.firstHighlightAt() + "_" + item.feedItemId();
        return Base64.getEncoder().encodeToString(cursorData.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * cursor 파싱 (Base64 디코딩 후 timestamp와 id 분리)
     */
    private CursorInfo parseCursor(String cursor) {
        if (cursor == null || cursor.trim().isEmpty()) {
            return new CursorInfo(null, null);
        }

        try {
            String decodedCursor = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = decodedCursor.split("_"); // 커서의 값은 firstHighlightAt_id 형태로 넘어옴

            if (parts.length != 2) {
                log.warn("Invalid cursor format: {}", cursor);
                return new CursorInfo(null, null);
            }

            LocalDateTime firstHighlightAt = LocalDateTime.parse(parts[0]);
            Long id = Long.parseLong(parts[1]);

            return new CursorInfo(firstHighlightAt, id);
        } catch (Exception e) {
            log.warn("Failed to parse cursor: {}, error: {}", cursor, e.getMessage());
            return new CursorInfo(null, null);
        }
    }
}
