ALTER TABLE drops
    RENAME COLUMN top_level_reply_count TO reply_count;

ALTER TABLE drops
    RENAME CONSTRAINT ck_drops_top_level_reply_count_non_negative
    TO ck_drops_reply_count_non_negative;

UPDATE drops AS d
SET reply_count = (
    SELECT COUNT(*)
    FROM drop_replies AS r
    WHERE r.drop_id = d.id
      AND r.deleted_at IS NULL
);

COMMENT ON COLUMN drops.reply_count IS
    'Cached count of all active top-level and second-level replies for this drop.';
