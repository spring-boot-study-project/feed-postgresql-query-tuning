-- 1. Users 테이블에 100만개 데이터 삽입
INSERT INTO
    users (
        username,
        email,
        nick_name,
        profile_image_url,
        status,
        created_at,
        updated_at
    )
SELECT
    'user_' || i,
    'user_' || i || '@example.com',
    'User ' || i,
    'https://example.com/profile/' || i || '.jpg',
    CASE
        WHEN i % 100 = 0 THEN 'INACTIVE'
        WHEN i % 1000 = 0 THEN 'SUSPENDED'
        ELSE 'ACTIVE'
    END,
    CURRENT_TIMESTAMP - (
        random() * interval '365 days'
    ),
    CURRENT_TIMESTAMP - (random() * interval '30 days')
FROM generate_series(1, 1000000) AS i;

-- 2. Pages 테이블에 100만개 데이터 삽입
INSERT INTO
    pages (
        url,
        title,
        description,
        domain,
        favicon_url,
        og_image_url,
        status,
        created_at,
        updated_at
    )
SELECT
    'https://example' || (i % 1000) || '.com/page/' || i,
    'Page Title ' || i,
    'This is a description for page ' || i,
    'example' || (i % 1000) || '.com',
    'https://example' || (i % 1000) || '.com/favicon.ico',
    'https://example' || (i % 1000) || '.com/og-image.jpg',
    CASE
        WHEN i % 500 = 0 THEN 'INACTIVE'
        WHEN i % 10000 = 0 THEN 'BLOCKED'
        ELSE 'ACTIVE'
    END,
    CURRENT_TIMESTAMP - (
        random() * interval '365 days'
    ),
    CURRENT_TIMESTAMP - (random() * interval '30 days')
FROM generate_series(1, 1000000) AS i;

-- 3. Highlights 테이블에 100만개 데이터 삽입
INSERT INTO
    highlights (
        user_id,
        page_id,
        text,
        color,
        visibility,
        start_offset,
        end_offset,
        context_before,
        context_after,
        created_at,
        updated_at
    )
SELECT ((i - 1) % 1000) + 1, -- user_id (1~1000000)
    i, -- page_id (1~1000000)
    'This is highlight text number ' || i,
    CASE (i % 7)
        WHEN 0 THEN 'YELLOW'
        WHEN 1 THEN 'GREEN'
        WHEN 2 THEN 'BLUE'
        WHEN 3 THEN 'PINK'
        WHEN 4 THEN 'PURPLE'
        WHEN 5 THEN 'ORANGE'
        ELSE 'RED'
    END,
    CASE
        WHEN i % 10 = 0 THEN 'PRIVATE'
        WHEN i % 100 = 0 THEN 'MENTIONED'
        ELSE 'PUBLIC'
    END,
    (i % 1000) + 1, -- start_offset
    (i % 1000) + 50, -- end_offset
    'Context before highlight ' || i,
    'Context after highlight ' || i,
    CURRENT_TIMESTAMP - (
        random() * interval '365 days'
    ),
    CURRENT_TIMESTAMP - (random() * interval '30 days')
FROM generate_series(1, 1000000) AS i;

-- 4. Feed Items 테이블에 100만개 데이터 삽입
-- 각 사용자-페이지 조합이 유니크해야 하므로 조심스럽게 생성
INSERT INTO
    feed_items (
        user_id,
        page_id,
        first_highlight_at,
        last_highlight_at,
        highlight_count,
        visibility,
        created_at,
        updated_at
    )
SELECT ((i - 1) % 1000) + 1, -- user_id (1~1000, 반복)
    i, -- page_id (1~1000000, 유니크)
    CURRENT_TIMESTAMP - (
        random() * interval '365 days'
    ),
    CURRENT_TIMESTAMP - (random() * interval '30 days'),
    (random() * 50)::integer + 1, -- highlight_count (1~50)
    CASE
        WHEN i % 10 = 0 THEN 'PRIVATE'
        WHEN i % 100 = 0 THEN 'MENTIONED'
        ELSE 'PUBLIC'
    END,
    CURRENT_TIMESTAMP - (
        random() * interval '365 days'
    ),
    CURRENT_TIMESTAMP - (random() * interval '30 days')
FROM generate_series(1, 1000000) AS i;

-- 5. Mentions 테이블에 100만개 데이터 삽입
INSERT INTO
    mentions (
        highlight_id,
        mentioned_user_id,
        created_at,
        updated_at
    )
SELECT i, -- highlight_id (1~1000000)
    ((i - 1) % 1000000) + 1, -- mentioned_user_id (1~1000000, 순환)
    CURRENT_TIMESTAMP - (
        random() * interval '365 days'
    ), CURRENT_TIMESTAMP - (random() * interval '30 days')
FROM generate_series(1, 1000000) AS i;
