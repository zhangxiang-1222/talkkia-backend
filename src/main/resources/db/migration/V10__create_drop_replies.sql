CREATE TYPE drop_reply_reaction_type AS ENUM ('like', 'dislike');

ALTER TABLE drops
    ADD COLUMN top_level_reply_count BIGINT NOT NULL DEFAULT 0,
    ADD CONSTRAINT ck_drops_top_level_reply_count_non_negative
        CHECK (top_level_reply_count >= 0);

CREATE TABLE drop_replies (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    drop_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_reply_id BIGINT,
    content TEXT NOT NULL,
    like_count BIGINT NOT NULL DEFAULT 0,
    dislike_count BIGINT NOT NULL DEFAULT 0,
    child_reply_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_drop_replies_drop
        FOREIGN KEY (drop_id) REFERENCES drops (id) ON DELETE CASCADE,
    CONSTRAINT fk_drop_replies_parent
        FOREIGN KEY (parent_reply_id) REFERENCES drop_replies (id) ON DELETE CASCADE,
    CONSTRAINT ck_drop_replies_user_id
        CHECK (user_id > 0),
    CONSTRAINT ck_drop_replies_content
        CHECK (
            char_length(btrim(content)) > 0
            AND char_length(content) <= 1000
        ),
    CONSTRAINT ck_drop_replies_counts_non_negative
        CHECK (
            like_count >= 0
            AND dislike_count >= 0
            AND child_reply_count >= 0
        ),
    CONSTRAINT ck_drop_replies_second_level_child_count
        CHECK (parent_reply_id IS NULL OR child_reply_count = 0)
);

CREATE FUNCTION validate_drop_reply_hierarchy()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_parent_drop_id BIGINT;
    v_grandparent_reply_id BIGINT;
    v_parent_deleted_at TIMESTAMPTZ;
BEGIN
    IF TG_OP = 'UPDATE'
       AND (
           NEW.drop_id IS DISTINCT FROM OLD.drop_id
           OR NEW.parent_reply_id IS DISTINCT FROM OLD.parent_reply_id
       ) THEN
        RAISE EXCEPTION 'Reply hierarchy cannot be changed after creation'
            USING ERRCODE = '23514';
    END IF;

    IF NEW.parent_reply_id IS NULL THEN
        RETURN NEW;
    END IF;

    IF NEW.id IS NOT NULL AND NEW.parent_reply_id = NEW.id THEN
        RAISE EXCEPTION 'A reply cannot be its own parent'
            USING ERRCODE = '23514';
    END IF;

    SELECT drop_id, parent_reply_id, deleted_at
    INTO v_parent_drop_id, v_grandparent_reply_id, v_parent_deleted_at
    FROM drop_replies
    WHERE id = NEW.parent_reply_id
    FOR SHARE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Parent reply % does not exist', NEW.parent_reply_id
            USING ERRCODE = '23503';
    END IF;

    IF v_parent_drop_id <> NEW.drop_id THEN
        RAISE EXCEPTION 'Parent reply must belong to the same drop'
            USING ERRCODE = '23514';
    END IF;

    IF v_grandparent_reply_id IS NOT NULL THEN
        RAISE EXCEPTION 'Replies can have at most two levels'
            USING ERRCODE = '23514';
    END IF;

    IF v_parent_deleted_at IS NOT NULL THEN
        RAISE EXCEPTION 'Cannot reply to a deleted reply'
            USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_validate_drop_reply_hierarchy
BEFORE INSERT OR UPDATE ON drop_replies
FOR EACH ROW
EXECUTE FUNCTION validate_drop_reply_hierarchy();

CREATE INDEX idx_drop_replies_top_level_feed
    ON drop_replies (drop_id, created_at DESC, id DESC)
    WHERE parent_reply_id IS NULL AND deleted_at IS NULL;

CREATE INDEX idx_drop_replies_second_level_preview
    ON drop_replies (parent_reply_id, created_at ASC, id ASC)
    WHERE parent_reply_id IS NOT NULL AND deleted_at IS NULL;

CREATE TABLE drop_reply_reactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reply_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reaction drop_reply_reaction_type NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_drop_reply_reactions_reply
        FOREIGN KEY (reply_id) REFERENCES drop_replies (id) ON DELETE CASCADE,
    CONSTRAINT uq_drop_reply_reactions_reply_user
        UNIQUE (reply_id, user_id),
    CONSTRAINT ck_drop_reply_reactions_user_id
        CHECK (user_id > 0)
);

COMMENT ON COLUMN drops.top_level_reply_count IS
    'Cached count of active top-level replies for this drop.';

COMMENT ON TABLE drop_replies IS
    'Top-level and second-level text replies for audio drops.';

COMMENT ON COLUMN drop_replies.parent_reply_id IS
    'NULL for top-level replies; references a top-level reply for second-level replies.';

COMMENT ON COLUMN drop_replies.child_reply_count IS
    'Cached count of active second-level replies; always zero for second-level replies.';

COMMENT ON TABLE drop_reply_reactions IS
    'One like or dislike state per user and reply.';
