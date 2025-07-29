package com.postgresql.feed.service;

import java.nio.charset.StandardCharsets;
// import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
// import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
// import java.util.function.Function;
import java.util.stream.Collectors;

// import org.springframework.data.redis.core.RedisTemplate;
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
    // private final RedisTemplate<String, Object> objectRedisTemplate;

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

    // private List<FeedItemDto> getFeedItemsWithUserAndPage(Long userId, int limit, LocalDateTime cursorFirstHighlightAt, Long cursorId) {
    //     // 첫 페이지만 2단계 캐시 전략 적용
    //     if (cursorFirstHighlightAt == null && cursorId == null && limit == 21) { // size=20 + 1
    //         return getFirstPageWithOptimizedCache(userId, limit);
    //     }
        
    //     // 첫 페이지가 아닌 경우 캐시 없이 DB 조회
    //     log.info("[CACHE DEBUG] No cache - Not first page (cursorFirstHighlightAt: {}, cursorId: {})", cursorFirstHighlightAt, cursorId);
    //     return feedItemRepository.findFeedItemsWithUserAndPage(userId, limit, cursorFirstHighlightAt, cursorId);
    // }
    
    // /**
    //  * 첫 페이지 조회 시 최적화된 2단계 캐시 전략 적용
    //  * 1단계: PUBLIC 피드 전역 캐시에서 조회
    //  * 2단계: 사용자별 PRIVATE/MENTIONED 피드 추가 조회
    //  * 3단계: 병합 후 정렬하여 최종 결과 반환
    //  */
    // private List<FeedItemDto> getFirstPageWithOptimizedCache(Long userId, int limit) {
    //     // 1단계: PUBLIC 피드 전역 캐시 조회 (충분한 여유분 확보)
    //     List<FeedItemDto> publicFeeds = getPublicFeedsFromCache();
        
    //     // 2단계: 사용자별 PRIVATE/MENTIONED 피드 조회
    //     List<FeedItemDto> privateFeeds = getPrivateAndMentionedFeeds(userId);
        
    //     // 3단계: 병합 및 정렬 후 제한된 개수 반환
    //     return mergeAndLimitFeeds(publicFeeds, privateFeeds, limit);
    // }
    
    // /**
    //  * PUBLIC 피드를 전역 캐시에서 조회
    //  * 모든 사용자가 공유하는 캐시로 캐시 효율성 극대화
    //  */
    // private List<FeedItemDto> getPublicFeedsFromCache() {
    //     String publicCacheKey = "public_feeds_50"; // 충분한 여유분으로 50개 캐시
        
    //     @SuppressWarnings("unchecked")
    //     List<FeedItemDto> cachedPublicFeeds = (List<FeedItemDto>) objectRedisTemplate.opsForValue().get(publicCacheKey);
        
    //     if (cachedPublicFeeds != null) {
    //         log.info("[CACHE DEBUG] PUBLIC Cache HIT - Retrieved {} public feeds from global cache", cachedPublicFeeds.size());
    //         return cachedPublicFeeds;
    //     }
        
    //     log.info("[CACHE DEBUG] PUBLIC Cache MISS - Querying database for public feeds");
    //     List<FeedItemDto> publicFeeds = feedItemRepository.findPublicFeedsOnly(50);
        
    //     // 전역 캐시에 저장 (10분 TTL - 더 긴 캐시 시간)
    //     objectRedisTemplate.opsForValue().set(publicCacheKey, publicFeeds, Duration.ofMinutes(10));
    //     log.info("[CACHE DEBUG] PUBLIC Cache STORED - Saved {} public feeds to global cache", publicFeeds.size());
        
    //     return publicFeeds;
    // }
    
    // /**
    //  * 사용자별 Private/Mentioned 피드 조회 (분리된 캐시 전략)
    //  * 각각 다른 무효화 조건과 TTL을 가질 수 있도록 분리
    //  */
    // private List<FeedItemDto> getPrivateAndMentionedFeeds(Long userId) {
    //     // 1. Private 피드 조회 (사용자 본인의 피드)
    //     List<FeedItemDto> privateFeeds = getPrivateFeeds(userId);
        
    //     // 2. Mentioned 피드 조회 (다른 사용자가 멘션한 피드)
    //     List<FeedItemDto> mentionedFeeds = getMentionedFeeds(userId);
        
    //     // 3. 병합 및 정렬
    //     List<FeedItemDto> mergedFeeds = new ArrayList<>();
    //     mergedFeeds.addAll(privateFeeds);
    //     mergedFeeds.addAll(mentionedFeeds);
        
    //     // firstHighlightAt 기준 내림차순 정렬
    //     mergedFeeds.sort((a, b) -> {
    //         int timeCompare = b.firstHighlightAt().compareTo(a.firstHighlightAt());
    //         if (timeCompare != 0) {
    //             return timeCompare;
    //         }
    //         return b.feedItemId().compareTo(a.feedItemId());
    //     });
        
    //     log.info("[CACHE DEBUG] Merged private/mentioned feeds: {} private + {} mentioned = {} total for userId: {}", 
    //             privateFeeds.size(), mentionedFeeds.size(), mergedFeeds.size(), userId);
        
    //     return mergedFeeds;
    // }
    
    // /**
    //  * Private 피드 조회 (캐시 적용)
    //  * 무효화 조건: 해당 사용자가 새 하이라이트 추가 시
    //  * TTL: 10분 (본인 피드는 상대적으로 안정적)
    //  */
    // private List<FeedItemDto> getPrivateFeeds(Long userId) {
    //     String cacheKey = "private_feeds:" + userId;
        
    //     @SuppressWarnings("unchecked")
    //     List<FeedItemDto> cachedFeeds = (List<FeedItemDto>) objectRedisTemplate.opsForValue().get(cacheKey);
        
    //     if (cachedFeeds != null) {
    //         log.info("[CACHE DEBUG] PRIVATE Cache HIT - Retrieved {} private feeds from cache for userId: {}", cachedFeeds.size(), userId);
    //         return cachedFeeds;
    //     }
        
    //     log.info("[CACHE DEBUG] PRIVATE Cache MISS - Querying private feeds for userId: {}", userId);
    //     List<FeedItemDto> feeds = feedItemRepository.findPrivateFeedsOnly(userId, 20);
        
    //     // 캐시에 저장 (10분 TTL - 본인 피드는 상대적으로 안정적)
    //     objectRedisTemplate.opsForValue().set(cacheKey, feeds, Duration.ofMinutes(10));
    //     log.info("[CACHE DEBUG] PRIVATE Cache STORED - Saved {} private feeds to cache for userId: {}", feeds.size(), userId);
        
    //     return feeds;
    // }
    
    // /**
    //  * Mentioned 피드 조회 (캐시 적용)
    //  * 무효화 조건: 다른 사용자가 해당 사용자를 멘션 시
    //  * TTL: 5분 (멘션은 더 자주 변경될 수 있음)
    //  */
    // private List<FeedItemDto> getMentionedFeeds(Long userId) {
    //     String cacheKey = "mentioned_feeds:" + userId;
        
    //     @SuppressWarnings("unchecked")
    //     List<FeedItemDto> cachedFeeds = (List<FeedItemDto>) objectRedisTemplate.opsForValue().get(cacheKey);
        
    //     if (cachedFeeds != null) {
    //         log.info("[CACHE DEBUG] MENTIONED Cache HIT - Retrieved {} mentioned feeds from cache for userId: {}", cachedFeeds.size(), userId);
    //         return cachedFeeds;
    //     }
        
    //     log.info("[CACHE DEBUG] MENTIONED Cache MISS - Querying mentioned feeds for userId: {}", userId);
    //     List<FeedItemDto> feeds = feedItemRepository.findMentionedFeedsOnly(userId, 20);
        
    //     // 캐시에 저장 (5분 TTL - 멘션은 더 자주 변경될 수 있음)
    //     objectRedisTemplate.opsForValue().set(cacheKey, feeds, Duration.ofMinutes(5));
    //     log.info("[CACHE DEBUG] MENTIONED Cache STORED - Saved {} mentioned feeds to cache for userId: {}", feeds.size(), userId);
        
    //     return feeds;
    // }
    
    // /**
    //  * PUBLIC 피드와 PRIVATE/MENTIONED 피드를 병합하고 정렬
    //  * firstHighlightAt 기준 내림차순 정렬 후 제한된 개수 반환
    //  */
    // private List<FeedItemDto> mergeAndLimitFeeds(List<FeedItemDto> publicFeeds, List<FeedItemDto> privateFeeds, int limit) {
    //     List<FeedItemDto> mergedFeeds = new ArrayList<>();
    //     mergedFeeds.addAll(publicFeeds);
    //     mergedFeeds.addAll(privateFeeds);
        
    //     // firstHighlightAt 기준 내림차순 정렬 (최신순)
    //     mergedFeeds.sort((a, b) -> {
    //         int timeCompare = b.firstHighlightAt().compareTo(a.firstHighlightAt());
    //         if (timeCompare != 0) {
    //             return timeCompare;
    //         }
    //         // firstHighlightAt이 같으면 id 기준 내림차순
    //         return b.feedItemId().compareTo(a.feedItemId());
    //     });
        
    //     // 중복 제거 (같은 feedItemId가 PUBLIC과 PRIVATE에 모두 있을 수 있음)
    //     List<FeedItemDto> uniqueFeeds = mergedFeeds.stream()
    //             .collect(Collectors.toMap(
    //                 FeedItemDto::feedItemId,
    //                 Function.identity(),
    //                 (existing, replacement) -> existing, // 중복 시 첫 번째 유지 (이미 정렬됨)
    //                 LinkedHashMap::new // 순서 유지
    //             ))
    //             .values()
    //             .stream()
    //             .collect(Collectors.toList());
        
    //     log.info("[CACHE DEBUG] Merged feeds: {} public + {} private = {} total, {} unique, returning {}", 
    //             publicFeeds.size(), privateFeeds.size(), mergedFeeds.size(), uniqueFeeds.size(), 
    //             Math.min(limit, uniqueFeeds.size()));
        
    //     // 요청된 개수만큼 반환
    //     return uniqueFeeds.stream()
    //             .limit(limit)
    //             .collect(Collectors.toList());
    // }

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
        
        // 하이라이트 조회 (캐시 적용)
        List<HighlightDto> highlights = feedItemRepository.findHighlightsByPages(pageIds);

        // 페이지 ID별로 하이라이트 그룹화
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

    /**
     * 페이지 ID 목록으로 하이라이트 조회 (개별 캐싱 적용)
     * 
     * @param pageIds 페이지 ID 목록
     * @return 하이라이트 DTO 목록
     */
    // private List<HighlightDto> getHighlightsByPageIds(List<Long> pageIds) {
    //     if (pageIds.isEmpty()) {
    //         return Collections.emptyList();
    //     }

    //     List<HighlightDto> allHighlights = new ArrayList<>();
    //     List<Long> uncachedPageIds = new ArrayList<>();

    //     // 개별 페이지 ID별로 캐시 확인
    //     for (Long pageId : pageIds) {
    //         String cacheKey = "highlights:pageId:" + pageId;
    //         @SuppressWarnings("unchecked")
    //         List<HighlightDto> cachedHighlights = (List<HighlightDto>) objectRedisTemplate.opsForValue().get(cacheKey);
            
    //         if (cachedHighlights != null) {
    //             log.debug("Cache HIT for highlights pageId: {}", pageId);
    //             allHighlights.addAll(cachedHighlights);
    //         } else {
    //             log.debug("Cache MISS for highlights pageId: {}", pageId);
    //             uncachedPageIds.add(pageId);
    //         }
    //     }

    //     // 캐시되지 않은 페이지 ID들에 대해 DB 조회
    //     if (!uncachedPageIds.isEmpty()) {
    //         List<HighlightDto> dbHighlights = feedItemRepository.findHighlightsByPages(uncachedPageIds);
            
    //         // 페이지 ID별로 그룹화하여 개별 캐싱
    //         Map<Long, List<HighlightDto>> highlightsByPageId = dbHighlights.stream()
    //                 .collect(Collectors.groupingBy(HighlightDto::getFeedItemId));
            
    //         // 각 페이지 ID별로 캐시 저장
    //         for (Long pageId : uncachedPageIds) {
    //             List<HighlightDto> pageHighlights = highlightsByPageId.getOrDefault(pageId, Collections.emptyList());
    //             String cacheKey = "highlights:pageId:" + pageId;
    //             objectRedisTemplate.opsForValue().set(cacheKey, pageHighlights, Duration.ofMinutes(15));
    //             allHighlights.addAll(pageHighlights);
    //         }
    //     }

    //     return allHighlights;
    // }
}
