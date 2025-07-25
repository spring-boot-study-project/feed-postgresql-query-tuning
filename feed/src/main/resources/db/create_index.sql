-- 1. User 엔티티 인덱스
-- 없음

-- 2. Page 엔티티 인덱스
-- 없음

-- 3. Highlight 엔티티 인덱스
CREATE INDEX idx_highlight_page_created ON highlights(page_id, created_at DESC);
CREATE INDEX idx_highlight_user_page_created ON highlights(user_id, page_id, created_at DESC);

-- 4. FeedItem 엔티티 인덱스
CREATE INDEX idx_feed_user_first_highlight ON feed_items(user_id, first_highlight_at DESC);
CREATE INDEX idx_feed_page ON feed_items(page_id);
CREATE INDEX idx_feed_visibility ON feed_items (visibility);
CREATE INDEX idx_feed_visibility_user ON feed_items(visibility, user_id);

-- 5. Mention 엔티티 인덱스
CREATE UNIQUE INDEX idx_mention_highlight_user ON mentions(highlight_id, mentioned_user_id);
