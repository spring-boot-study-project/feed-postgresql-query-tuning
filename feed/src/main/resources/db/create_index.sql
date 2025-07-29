-- ========================================
-- 추가 성능 최적화 인덱스
-- ========================================
-- 엔티티에서 정의할 수 없는 특수 인덱스들만 포함
-- 기본 인덱스들은 각 엔티티 클래스에서 @Index로 관리됨

-- 1. Highlight 엔티티 - 커버링 인덱스
-- 상세 하이라이트 조회 시 테이블 액세스 제거를 위한 커버링 인덱스
CREATE INDEX idx_highlight_page_created_covering ON highlights(page_id, created_at DESC) 
INCLUDE (id, text, color);

-- 2. FeedItem 엔티티 - 부분 인덱스 (WHERE 조건 포함)
-- 엔티티 @Index에서는 WHERE 조건을 지원하지 않으므로 별도 생성

-- PUBLIC 피드 조회 최적화 (visibility='PUBLIC' AND highlight_count > 0)```
CREATE INDEX idx_feed_public_optimized_partial ON feed_items(visibility, highlight_count, first_highlight_at DESC, id DESC) 
WHERE visibility = 'PUBLIC' AND highlight_count > 0;

-- PRIVATE 피드 조회 최적화 (visibility='PRIVATE' AND highlight_count > 0)  
CREATE INDEX idx_feed_private_optimized_partial ON feed_items(user_id, visibility, highlight_count, first_highlight_at DESC, id DESC) 
WHERE visibility = 'PRIVATE' AND highlight_count > 0;

-- MENTIONED 피드 최적화 (visibility='MENTIONED' AND highlight_count > 0)
CREATE INDEX idx_feed_mentioned_optimized_partial ON feed_items(page_id, visibility, highlight_count, first_highlight_at DESC, id DESC) 
WHERE visibility = 'MENTIONED' AND highlight_count > 0;

-- 3. OR 조건 최적화를 위한 통합 인덱스
-- 여러 visibility 조건을 OR로 검사하는 쿼리를 위한 인덱스
-- highlight_count > 0 조건과 정렬을 함께 처리
CREATE INDEX idx_feed_items_highlight_sort_partial ON feed_items(first_highlight_at DESC, id DESC) 
WHERE highlight_count > 0;

-- 4. 추가 최적화: visibility별 정렬 인덱스 (OR 조건 대응)
CREATE INDEX idx_feed_items_visibility_sort ON feed_items(visibility, first_highlight_at DESC, id DESC) 
WHERE highlight_count > 0;
