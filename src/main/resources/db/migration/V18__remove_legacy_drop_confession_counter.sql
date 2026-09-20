ALTER TABLE drops
    DROP CONSTRAINT ck_drops_engagement_counters_non_negative,
    DROP COLUMN confession,
    ADD CONSTRAINT ck_drops_engagement_counters_non_negative
        CHECK (
            funny >= 0
            AND opinion >= 0
            AND student >= 0
            AND random >= 0
            AND listen >= 0
        );
