-- 1. 사용자 테이블
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    nick_name VARCHAR(100) NOT NULL,
    profile_image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. 웹페이지 테이블
CREATE TABLE pages (
    id BIGSERIAL PRIMARY KEY,
    url VARCHAR(2000) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description VARCHAR(1000),
    domain VARCHAR(200) NOT NULL,
    favicon_url VARCHAR(500),
    og_image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. 하이라이트 테이블
CREATE TABLE highlights (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    page_id BIGINT NOT NULL REFERENCES pages(id),
    text VARCHAR(500) NOT NULL,
    color VARCHAR(20) NOT NULL DEFAULT 'YELLOW' CHECK (color IN ('YELLOW', 'GREEN', 'BLUE', 'PINK', 'PURPLE', 'ORANGE', 'RED')),
    visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC' CHECK (visibility IN ('PUBLIC', 'PRIVATE', 'MENTIONED')),
    start_offset INTEGER NOT NULL,
    end_offset INTEGER NOT NULL,
    context_before VARCHAR(500),
    context_after VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. 피드 아이템 테이블
CREATE TABLE feed_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    page_id BIGINT NOT NULL REFERENCES pages(id),
    first_highlight_at TIMESTAMP NOT NULL,
    last_highlight_at TIMESTAMP NOT NULL,
    highlight_count INTEGER NOT NULL DEFAULT 0,
    visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC' CHECK (visibility IN ('PUBLIC', 'PRIVATE', 'MENTIONED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, page_id)
);

-- 5. 멘션 테이블
CREATE TABLE mentions (
    id BIGSERIAL PRIMARY KEY,
    highlight_id BIGINT NOT NULL REFERENCES highlights(id),
    mentioned_user_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(highlight_id, mentioned_user_id)
);
