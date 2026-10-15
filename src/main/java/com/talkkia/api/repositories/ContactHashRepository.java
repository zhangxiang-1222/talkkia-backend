package com.talkkia.api.repositories;

import com.talkkia.api.model.contact.ContactHashRecord;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

@Repository
public class ContactHashRepository {

    private final JdbcTemplate jdbcTemplate;

    public ContactHashRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void upsertContacts(Long ownerId, Collection<ContactHashRecord> contacts) {
        if (contacts.isEmpty()) {
            return;
        }

        List<Object[]> batchArgs = contacts.stream()
                .map(contact -> new Object[]{
                        ownerId,
                        contact.getPhoneHmac(),
                        contact.getPhoneCiphertext(),
                        contact.getEncryptionNonce(),
                        contact.getSyncToken()
                })
                .collect(Collectors.toList());
        jdbcTemplate.batchUpdate(
                "INSERT INTO contact_hashes (owner_id, phone_hmac, phone_ciphertext, encryption_nonce, sync_token) "
                        + "VALUES (?, ?, ?, ?, ?) "
                        + "ON CONFLICT (owner_id, phone_hmac) "
                        + "DO UPDATE SET last_seen_at = CURRENT_TIMESTAMP",
                batchArgs
        );
    }

    public List<UUID> removeContactsNotIn(Long ownerId, Collection<String> phoneHmacs) {
        if (phoneHmacs.isEmpty()) {
            return jdbcTemplate.query(
                    "DELETE FROM contact_hashes WHERE owner_id = ? RETURNING sync_token",
                    (resultSet, rowNum) -> resultSet.getObject("sync_token", UUID.class),
                    ownerId
            );
        }

        String placeholders = placeholders(phoneHmacs.size());
        List<Object> parameters = new ArrayList<>();
        parameters.add(ownerId);
        parameters.addAll(phoneHmacs);
        return jdbcTemplate.query(
                "DELETE FROM contact_hashes WHERE owner_id = ? AND phone_hmac NOT IN (" + placeholders + ") "
                        + "RETURNING sync_token",
                (resultSet, rowNum) -> resultSet.getObject("sync_token", UUID.class),
                parameters.toArray()
        );
    }

    public List<UUID> removeContactsBySyncTokens(Long ownerId, Collection<UUID> syncTokens) {
        if (syncTokens.isEmpty()) {
            return new ArrayList<>();
        }

        String placeholders = placeholders(syncTokens.size());
        List<Object> parameters = new ArrayList<>();
        parameters.add(ownerId);
        parameters.addAll(syncTokens);
        return jdbcTemplate.query(
                "DELETE FROM contact_hashes WHERE owner_id = ? AND sync_token IN (" + placeholders + ") "
                        + "RETURNING sync_token",
                (resultSet, rowNum) -> resultSet.getObject("sync_token", UUID.class),
                parameters.toArray()
        );
    }

    public Map<String, UUID> findSyncTokens(Long ownerId, Collection<String> phoneHmacs) {
        Map<String, UUID> syncTokens = new LinkedHashMap<>();
        if (phoneHmacs.isEmpty()) {
            return syncTokens;
        }

        String placeholders = placeholders(phoneHmacs.size());
        List<Object> parameters = new ArrayList<>();
        parameters.add(ownerId);
        parameters.addAll(phoneHmacs);
        jdbcTemplate.query(
                "SELECT phone_hmac, sync_token FROM contact_hashes "
                        + "WHERE owner_id = ? AND phone_hmac IN (" + placeholders + ")",
                (RowCallbackHandler) resultSet -> syncTokens.put(
                        resultSet.getString("phone_hmac"),
                        resultSet.getObject("sync_token", UUID.class)
                ),
                parameters.toArray()
        );
        return syncTokens;
    }

    private String placeholders(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(value -> "?")
                .collect(Collectors.joining(", "));
    }
}
