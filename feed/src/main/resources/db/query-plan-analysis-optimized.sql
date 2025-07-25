-- 최적화된 쿼리 플랜 분석을 위한 EXPLAIN ANALYZE 코드
-- Terminal #717-825에서 실행된 개선된 쿼리들에 대한 상세 분석

-- ========================================
-- 첫 번째 쿼리: FeedItem ID 조회 쿼리 플랜 (최적화된 버전)
-- ========================================

EXPLAIN (
    ANALYZE,
    BUFFERS,
    FORMAT TEXT
)
select fi1_0.id
from feed_items fi1_0
where (
        fi1_0.visibility = 'PUBLIC'
        or fi1_0.visibility = 'PRIVATE'
        and fi1_0.user_id = 1
        or fi1_0.visibility = 'MENTIONED'
        and exists (
            select 1
            from
                mentions m1_0
                join highlights h1_0 on h1_0.id = m1_0.highlight_id
            where
                h1_0.page_id = fi1_0.page_id
                and m1_0.mentioned_user_id = 1
        )
    )
    and fi1_0.highlight_count > 0
order by fi1_0.first_highlight_at desc, fi1_0.id desc
fetch first
    21 rows only;

-- ========================================
-- 첫 번째 쿼리: IN 절을 사용한 피드 목록 조회 (최적화된 버전)
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
WHERE fi1_0.id IN (
    -- 실제 실행 시에는 구체적인 ID 값들이 들어감
    -- 예시: 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21
)
ORDER BY fi1_0.first_highlight_at DESC, fi1_0.id DESC;

-- ========================================
-- 두 번째 쿼리: ROW_NUMBER()를 사용한 하이라이트 조회 (윈도우 함수 활용)
-- ========================================

EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT
    h.id,
    h.text,
    h.color,
    h.created_at as createdAt,
    h.page_id as feedItemId
FROM (
    SELECT
        h.id,
        h.text,
        h.color,
        h.created_at,
        h.page_id,
        ROW_NUMBER() OVER (
            PARTITION BY h.page_id 
            ORDER BY h.created_at DESC
        ) as rn
    FROM highlights h
    WHERE h.page_id IN (
        -- 실제 실행 시에는 첫 번째 쿼리에서 반환된 page_id 값들이 들어감
        -- 예시 page_id 목록
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20
    )
) h
WHERE h.rn <= 3
ORDER BY h.page_id, h.rn;

-- ========================================
-- 성능 비교를 위한 기존 복잡한 쿼리 (참고용)
-- ========================================

-- 기존 복잡한 쿼리 (LEFT JOIN 사용)
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
LEFT JOIN highlights h1_0 ON h1_0.page_id = fi1_0.page_id AND h1_0.user_id = fi1_0.user_id
LEFT JOIN mentions m1_0 ON m1_0.highlight_id = h1_0.id
WHERE (
    fi1_0.visibility = 'PUBLIC' 
    OR (fi1_0.visibility = 'PRIVATE' AND u1_0.id = 1)
    OR (fi1_0.visibility = 'MENTIONED' AND m1_0.mentioned_user_id = 1)
)
GROUP BY 
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
ORDER BY fi1_0.first_highlight_at DESC, fi1_0.id DESC
LIMIT 20;

-- ========================================
-- 성능 분석을 위한 추가 정보
-- ========================================

-- 쿼리 실행 통계 비교
SELECT 
    'Optimized Query Performance' as analysis_type,
    '1ms execution time' as first_query_performance,
    '1ms execution time' as second_query_performance,
    '206ms total JDBC time' as total_performance;

-- 최적화 포인트 분석
SELECT 
    'Optimization Points' as category,
    'Removed complex LEFT JOINs' as improvement_1,
    'Used IN clause instead of complex WHERE conditions' as improvement_2,
    'Eliminated GROUP BY overhead' as improvement_3,
    'Used ROW_NUMBER() for efficient pagination' as improvement_4;

-- 인덱스 활용도 확인
SELECT 
    indexname,
    tablename,
    indexdef
FROM pg_indexes
WHERE tablename IN ('feed_items', 'highlights')
    AND indexname LIKE '%id%'
ORDER BY tablename, indexname;