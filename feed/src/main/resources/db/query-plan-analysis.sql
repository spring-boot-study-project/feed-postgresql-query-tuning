-- 쿼리 플랜 분석을 위한 EXPLAIN ANALYZE 코드
-- 실제 실행된 2개의 쿼리에 대한 상세 분석

-- ========================================
-- 첫 번째 쿼리: 피드 목록 조회 (복잡한 조인 쿼리)
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
-- 두 번째 쿼리: 하이라이트 조회 (단순 조회)
-- ========================================

EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT 
    h1_0.id,
    h1_0.content,
    h1_0.created_at,
    h1_0.page_id,
    h1_0.updated_at,
    h1_0.user_id
FROM highlights h1_0
WHERE h1_0.page_id IN (
    -- 첫 번째 쿼리에서 반환된 page_id 목록
    -- 실제 실행 시에는 구체적인 page_id 값들이 들어감
    SELECT DISTINCT fi1_0.page_id
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
        fi1_0.first_highlight_at,
        fi1_0.page_id,
        fi1_0.user_id,
        fi1_0.visibility,
        p1_0.id,
        p1_0.title,
        u1_0.id,
        u1_0.username
    ORDER BY fi1_0.first_highlight_at DESC, fi1_0.id DESC
    LIMIT 20
);

-- ========================================
-- 추가 분석을 위한 테이블 통계 정보
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