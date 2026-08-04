ALTER TABLE drop_replies
    DROP CONSTRAINT ck_drop_replies_content;

ALTER TABLE drop_replies
    RENAME COLUMN content TO audio_url;

ALTER TABLE drop_replies
    ADD COLUMN status drop_status NOT NULL DEFAULT 'init',
    ADD CONSTRAINT ck_drop_replies_audio_url
        CHECK (char_length(btrim(audio_url)) > 0);

UPDATE drops AS d
SET reply_count = (
    SELECT COUNT(*)
    FROM drop_replies AS r
    WHERE r.drop_id = d.id
      AND r.status = 'approved'::drop_status
      AND r.deleted_at IS NULL
);

UPDATE drop_replies AS parent
SET child_reply_count = (
    SELECT COUNT(*)
    FROM drop_replies AS child
    WHERE child.parent_reply_id = parent.id
      AND child.status = 'approved'::drop_status
      AND child.deleted_at IS NULL
)
WHERE parent.parent_reply_id IS NULL;

CREATE INDEX idx_drop_replies_pending_id
    ON drop_replies (id)
    WHERE status = 'pending'::drop_status
      AND deleted_at IS NULL;

COMMENT ON TABLE drop_replies IS
    'Top-level and second-level audio replies for approved audio drops.';

COMMENT ON COLUMN drop_replies.audio_url IS
    'Stable R2 object key for the reply audio.';

COMMENT ON COLUMN drop_replies.status IS
    'Upload and moderation lifecycle: init, pending, approved, or rejected.';
