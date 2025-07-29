-- 커넥션 풀 최적화 효과 검증 쿼리
-- 이 스크립트를 사용하여 최적화 전후 성능을 비교하세요

-- 1. 현재 PostgreSQL 커넥션 상태 확인
SELECT 
    state,
    COUNT(*) as connection_count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentage
FROM pg_stat_activity 
WHERE datname = 'feed-service'
GROUP BY state
ORDER BY connection_count DESC;

-- 2. 커넥션 풀 관련 통계
SELECT 
    'Max Connections' as metric,
    setting as value
FROM pg_settings 
WHERE name = 'max_connections'

UNION ALL

SELECT 
    'Current Connections' as metric,
    COUNT(*)::text as value
FROM pg_stat_activity 
WHERE datname = 'feed-service'

UNION ALL

SELECT 
    'Active Connections' as metric,
    COUNT(*)::text as value
FROM pg_stat_activity 
WHERE datname = 'feed-service' AND state = 'active'

UNION ALL

SELECT 
    'Idle Connections' as metric,
    COUNT(*)::text as value
FROM pg_stat_activity 
WHERE datname = 'feed-service' AND state = 'idle';

-- 3. 커넥션 대기 시간 분석 (최근 1시간)
SELECT 
    query_start,
    state_change,
    state,
    wait_event_type,
    wait_event,
    query,
    EXTRACT(EPOCH FROM (COALESCE(state_change, NOW()) - query_start)) as duration_seconds
FROM pg_stat_activity 
WHERE datname = 'feed-service'
    AND query_start > NOW() - INTERVAL '1 hour'
    AND state != 'idle'
ORDER BY duration_seconds DESC
LIMIT 10;

-- 4. 데이터베이스 성능 지표
SELECT 
    'Shared Buffers Hit Ratio' as metric,
    ROUND(
        100.0 * sum(blks_hit) / (sum(blks_hit) + sum(blks_read)), 2
    )::text || '%' as value
FROM pg_stat_database 
WHERE datname = 'feed-service'

UNION ALL

SELECT 
    'Total Transactions' as metric,
    (xact_commit + xact_rollback)::text as value
FROM pg_stat_database 
WHERE datname = 'feed-service'

UNION ALL

SELECT 
    'Transaction Commit Ratio' as metric,
    ROUND(100.0 * xact_commit / (xact_commit + xact_rollback), 2)::text || '%' as value
FROM pg_stat_database 
WHERE datname = 'feed-service';

-- 5. 슬로우 쿼리 분석 (pg_stat_statements 확장 필요)
-- 이 쿼리는 pg_stat_statements 확장이 설치된 경우에만 작동합니다
/*
SELECT 
    query,
    calls,
    ROUND(total_exec_time::numeric, 2) as total_time_ms,
    ROUND(mean_exec_time::numeric, 2) as avg_time_ms,
    ROUND((100 * total_exec_time / sum(total_exec_time) OVER())::numeric, 2) as percentage
FROM pg_stat_statements 
WHERE query LIKE '%feed_items%' OR query LIKE '%users%' OR query LIKE '%pages%'
ORDER BY total_exec_time DESC
LIMIT 10;
*/

-- 6. 테이블별 I/O 통계
SELECT 
    schemaname,
    tablename,
    seq_scan,
    seq_tup_read,
    idx_scan,
    idx_tup_fetch,
    n_tup_ins,
    n_tup_upd,
    n_tup_del
FROM pg_stat_user_tables 
WHERE schemaname = 'public'
ORDER BY seq_scan + idx_scan DESC;

-- 7. 인덱스 사용률 분석
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes 
WHERE schemaname = 'public'
ORDER BY idx_scan DESC;

-- 8. 커넥션 풀 성능 테스트 쿼리 (부하 테스트용)
-- 이 쿼리를 동시에 여러 번 실행하여 커넥션 풀 성능을 테스트하세요
\timing on

-- 간단한 SELECT 쿼리 (커넥션 획득 시간 측정용)
SELECT 1 as connection_test;

-- 실제 비즈니스 쿼리 (전체 응답 시간 측정용)
SELECT 
    fi.id,
    fi.first_highlight_at,
    u.username,
    p.title
FROM feed_items fi
JOIN users u ON fi.user_id = u.id
JOIN pages p ON fi.page_id = p.id
WHERE fi.visibility = 'PUBLIC'
    AND fi.highlight_count > 0
ORDER BY fi.first_highlight_at DESC
LIMIT 20;

\timing off