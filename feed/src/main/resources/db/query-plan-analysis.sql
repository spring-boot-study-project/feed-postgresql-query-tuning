-- 실제 실행된 쿼리에 대한 EXPLAIN ANALYZE 분석
-- 터미널 출력 (105-186 라인)에서 확인된 2개의 쿼리

-- ========================================
-- 첫 번째 쿼리: 피드 목록 조회 (EXISTS 서브쿼리 사용)
-- ========================================

EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT 
    fi1_0.id,
    u1_0.id,
    u1_0.username,
    u1_0.nick_name,
    p1_0.id,
    p1_0.url,
    p1_0.title,
    p1_0.domain,
    fi1_0.highlight_count,
    fi1_0.first_highlight_at
FROM feed_items fi1_0
JOIN users u1_0 ON u1_0.id = fi1_0.user_id
JOIN pages p1_0 ON p1_0.id = fi1_0.page_id
WHERE (
    fi1_0.visibility = 'PUBLIC'
    OR (fi1_0.visibility = 'PRIVATE' AND u1_0.id = 1)
    OR (fi1_0.visibility = 'MENTIONED' AND EXISTS(
        SELECT 1
        FROM mentions m1_0
        JOIN highlights h1_0 ON h1_0.id = m1_0.highlight_id
        WHERE h1_0.page_id = p1_0.id
        AND m1_0.mentioned_user_id = 1
    ))
)
AND fi1_0.highlight_count > 0
ORDER BY fi1_0.first_highlight_at DESC, fi1_0.id DESC
LIMIT 20;

-- ========================================
-- 두 번째 쿼리: 하이라이트 조회 (CTE와 윈도우 함수 사용)
-- ========================================

EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
WITH ranked_highlights AS (
    SELECT 
        h.id,
        h.text,
        h.color,
        h.created_at,
        h.page_id,
        ROW_NUMBER() OVER (PARTITION BY h.page_id ORDER BY h.created_at DESC) as rn
    FROM highlights h
    WHERE h.page_id IN (
        1, 2, 5, 4, 21, 6, 7, 8, 9, 10, 
        11, 12, 13, 22, 15, 16, 17, 18, 19, 20
    )
)
SELECT 
    id,
    text,
    color,
    created_at as createdAt,
    page_id as feedItemId
FROM ranked_highlights
WHERE rn <= 3
ORDER BY page_id, rn;


-- ========================================
-- 성능 분석을 위한 추가 쿼리들
-- ========================================

-- 각 테이블의 행 수 확인
SELECT 'feed_items' as table_name, COUNT(*) as row_count FROM feed_items
UNION ALL
SELECT 'users' as table_name, COUNT(*) as row_count FROM users
UNION ALL
SELECT 'pages' as table_name, COUNT(*) as row_count FROM pages
UNION ALL
SELECT 'highlights' as table_name, COUNT(*) as row_count FROM highlights
UNION ALL
SELECT 'mentions' as table_name, COUNT(*) as row_count FROM mentions;

-- 현재 인덱스 상태 확인
SELECT 
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename IN ('feed_items', 'users', 'pages', 'highlights', 'mentions')
ORDER BY tablename, indexname;

-- 테이블 크기 확인
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables
WHERE tablename IN ('feed_items', 'users', 'pages', 'highlights', 'mentions')
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
