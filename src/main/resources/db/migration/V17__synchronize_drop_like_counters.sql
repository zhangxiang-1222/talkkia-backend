UPDATE drops
SET like_count = GREATEST(like_count, confession),
    confession = GREATEST(like_count, confession);
