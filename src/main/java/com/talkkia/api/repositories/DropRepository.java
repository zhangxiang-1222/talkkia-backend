package com.talkkia.api.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import com.talkkia.api.dto.drop.DropListItem;
import com.talkkia.api.entity.Drop;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DropRepository {

    private static final String DROP_COLUMNS =
            "id, user_id, city_id, audio_url, status::text AS status, "
                    + "confession, funny, opinion, student, random, listen, "
                    + "reply_count, expires_at";

    private static final String DROP_LIST_COLUMNS =
            "d.id, d.user_id, d.city_id, c.city_name, d.audio_url, d.confession, d.funny, "
                    + "d.opinion, d.student, d.random, d.listen, d.reply_count, "
                    + "COALESCE(udr.liked, FALSE) AS liked, "
                    + "COALESCE(udr.laughed, FALSE) AS laughed, d.created_at";

    private final JdbcTemplate jdbcTemplate;

    public DropRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Drop createInitialDrop(Long userId, Long cityId, String audioUrl) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO drops (user_id, city_id, audio_url, status) "
                        + "VALUES (?, ?, ?, 'init'::drop_status) "
                        + "RETURNING " + DROP_COLUMNS,
                (resultSet, rowNum) -> mapDrop(resultSet),
                userId,
                cityId,
                audioUrl
        );
    }

    public boolean markUploadCompleted(Long userId, Long dropId) {
        int updatedCount = jdbcTemplate.update(
                "UPDATE drops SET status = 'pending'::drop_status "
                        + "WHERE id = ? AND user_id = ? "
                        + "AND status IN ('init'::drop_status, 'pending'::drop_status) "
                        + "AND expires_at > CURRENT_TIMESTAMP",
                dropId,
                userId
        );

        return updatedCount > 0;
    }

    public Drop findPendingDrop(Long dropId) {
        return jdbcTemplate.query(
                "SELECT " + DROP_COLUMNS + " "
                        + "FROM drops WHERE id = ? "
                        + "AND status = 'pending'::drop_status "
                        + "AND expires_at > CURRENT_TIMESTAMP",
                (resultSet, rowNum) -> mapDrop(resultSet),
                dropId
        ).stream().findFirst().orElse(null);
    }

    public boolean recordModerationResult(Long dropId, boolean approved) {
        String targetStatus = approved ? "approved" : "rejected";
        int updatedCount = jdbcTemplate.update(
                "UPDATE drops SET status = ?::drop_status "
                        + "WHERE id = ? "
                        + "AND status = 'pending'::drop_status "
                        + "AND expires_at > CURRENT_TIMESTAMP",
                targetStatus,
                dropId
        );

        return updatedCount > 0;
    }

    public boolean isOwnedAndAvailable(Long userId, Long dropId) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM drops WHERE id = ? AND user_id = ? "
                        + "AND expires_at > CURRENT_TIMESTAMP)",
                Boolean.class,
                dropId,
                userId
        );

        return Boolean.TRUE.equals(exists);
    }

    public int deleteOwnedDrop(Long userId, Long dropId) {
        return jdbcTemplate.update(
                "DELETE FROM drops WHERE id = ? AND user_id = ?",
                dropId,
                userId
        );
    }

    public Boolean findModerationDecision(Long dropId) {
        List<String> statuses = jdbcTemplate.query(
                "SELECT status::text FROM drops WHERE id = ? "
                        + "AND expires_at > CURRENT_TIMESTAMP",
                (resultSet, rowNum) -> resultSet.getString("status"),
                dropId
        );

        if (statuses.isEmpty()) {
            return null;
        }

        String status = statuses.get(0);
        if ("approved".equals(status)) {
            return true;
        }
        if ("rejected".equals(status)) {
            return false;
        }

        return null;
    }

    public List<DropListItem> findLatestAvailableDrops(
            Long userId,
            Long cityId,
            int limit,
            long offset
    ) {
        return jdbcTemplate.query(
                "SELECT " + DROP_LIST_COLUMNS + " "
                        + "FROM drops d JOIN cities c ON c.id = d.city_id "
                        + "LEFT JOIN user_drop_reactions udr "
                        + "ON udr.drop_id = d.id AND udr.user_id = ? "
                        + "WHERE d.city_id = ? "
                        + "AND d.status = 'approved'::drop_status "
                        + "AND d.expires_at > CURRENT_TIMESTAMP "
                        + "ORDER BY d.created_at ASC, d.id ASC LIMIT ? OFFSET ?",
                (resultSet, rowNum) -> new DropListItem(
                        resultSet.getLong("id"),
                        resultSet.getLong("user_id"),
                        null,
                        null,
                        resultSet.getLong("city_id"),
                        resultSet.getString("city_name"),
                        resultSet.getString("audio_url"),
                        resultSet.getLong("confession"),
                        resultSet.getLong("funny"),
                        resultSet.getLong("opinion"),
                        resultSet.getLong("student"),
                        resultSet.getLong("random"),
                        resultSet.getLong("listen"),
                        resultSet.getLong("reply_count"),
                        resultSet.getBoolean("liked"),
                        resultSet.getBoolean("laughed"),
                        getInstant(resultSet, "created_at")
                ),
                userId,
                cityId,
                limit,
                offset
        );
    }

    public long countLatestAvailableDrops(Long cityId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM drops WHERE city_id = ? "
                        + "AND status = 'approved'::drop_status "
                        + "AND expires_at > CURRENT_TIMESTAMP",
                Long.class,
                cityId
        );
        return count == null ? 0 : count;
    }

    public List<Drop> findLatestUserDrops(Long userId, int limit, long offset) {
        return jdbcTemplate.query(
                "SELECT " + DROP_COLUMNS + " "
                        + "FROM drops WHERE user_id = ? "
                        + "AND status <> 'init'::drop_status "
                        + "AND expires_at > CURRENT_TIMESTAMP "
                        + "ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?",
                (resultSet, rowNum) -> mapDrop(resultSet),
                userId,
                limit,
                offset
        );
    }

    public long countLatestUserDrops(Long userId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM drops WHERE user_id = ? "
                        + "AND status <> 'init'::drop_status "
                        + "AND expires_at > CURRENT_TIMESTAMP",
                Long.class,
                userId
        );
        return count == null ? 0 : count;
    }

    private Drop mapDrop(ResultSet resultSet) throws SQLException {
        OffsetDateTime expiresAt = resultSet.getObject("expires_at", OffsetDateTime.class);
        return new Drop(
                resultSet.getLong("id"),
                resultSet.getLong("user_id"),
                resultSet.getLong("city_id"),
                resultSet.getString("audio_url"),
                resultSet.getString("status"),
                resultSet.getLong("confession"),
                resultSet.getLong("funny"),
                resultSet.getLong("opinion"),
                resultSet.getLong("student"),
                resultSet.getLong("random"),
                resultSet.getLong("listen"),
                resultSet.getLong("reply_count"),
                expiresAt.toInstant()
        );
    }

    private Instant getInstant(ResultSet resultSet, String columnName) throws SQLException {
        OffsetDateTime value = resultSet.getObject(columnName, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }
}
