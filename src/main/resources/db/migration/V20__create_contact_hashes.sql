CREATE TABLE contact_hashes (
    owner_id BIGINT NOT NULL,
    phone_hmac VARCHAR(64) NOT NULL,
    phone_ciphertext BYTEA NOT NULL,
    encryption_nonce BYTEA NOT NULL,
    sync_token UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_contact_hashes PRIMARY KEY (owner_id, phone_hmac),
    CONSTRAINT uq_contact_hashes_sync_token UNIQUE (sync_token),
    CONSTRAINT ck_contact_hashes_phone_hmac_length CHECK (char_length(phone_hmac) = 64),
    CONSTRAINT ck_contact_hashes_nonce_length CHECK (octet_length(encryption_nonce) = 12)
);

CREATE INDEX idx_contact_hashes_phone_hmac ON contact_hashes (phone_hmac);
