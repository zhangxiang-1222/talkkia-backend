ALTER TABLE user_city_drop_progress
    DROP CONSTRAINT pk_user_city_drop_progress;

ALTER TABLE user_city_drop_progress
    ADD COLUMN id BIGINT GENERATED ALWAYS AS IDENTITY;

ALTER TABLE user_city_drop_progress
    ADD CONSTRAINT pk_user_city_drop_progress
        PRIMARY KEY (id),
    ADD CONSTRAINT uq_user_city_drop_progress_user_city
        UNIQUE (user_id, city_id);
