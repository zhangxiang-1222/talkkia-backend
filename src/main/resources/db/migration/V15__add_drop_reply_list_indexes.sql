DROP INDEX IF EXISTS idx_drop_replies_top_level_feed;
DROP INDEX IF EXISTS idx_drop_replies_second_level_preview;

CREATE INDEX idx_drop_replies_top_level_approved_latest
    ON drop_replies (drop_id, created_at DESC, id DESC)
    WHERE parent_reply_id IS NULL
      AND status = 'approved'::drop_status
      AND deleted_at IS NULL;

CREATE INDEX idx_drop_replies_top_level_approved_likes
    ON drop_replies (drop_id, like_count DESC, created_at DESC, id DESC)
    WHERE parent_reply_id IS NULL
      AND status = 'approved'::drop_status
      AND deleted_at IS NULL;

CREATE INDEX idx_drop_replies_top_level_approved_discussed
    ON drop_replies (drop_id, child_reply_count DESC, created_at DESC, id DESC)
    WHERE parent_reply_id IS NULL
      AND status = 'approved'::drop_status
      AND deleted_at IS NULL;

CREATE INDEX idx_drop_replies_children_approved
    ON drop_replies (parent_reply_id, created_at ASC, id ASC)
    WHERE parent_reply_id IS NOT NULL
      AND status = 'approved'::drop_status
      AND deleted_at IS NULL;
