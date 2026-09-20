package com.talkkia.api.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

import com.talkkia.api.dto.reply.ReplyListItem;
import com.talkkia.api.dto.reply.ReplyReactionResponse;
import com.talkkia.api.entity.DropReply;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DropReplyRepository {

    private static final String REPLY_COLUMNS =
            "id, drop_id, user_id, parent_reply_id, audio_url, status::text AS status, "
                    + "like_count, dislike_count, child_reply_count, "
                    + "created_at, updated_at, deleted_at";

    private static final String REPLY_LIST_COLUMNS =
            "r.id, r.user_id, r.audio_url, r.like_count, r.dislike_count, "
                    + "reaction.reaction::text AS reaction, "
                    + "(r.user_id = d.user_id) AS drop_author, "
                    + "r.child_reply_count, r.created_at";

    private final JdbcTemplate jdbcTemplate;

    public DropReplyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DropReply createInitialReply(
            Long userId,
            Long dropId,
            Long parentReplyId,
            String audioUrl
    ) {
        if (parentReplyId == null) {
            return jdbcTemplate.query(
                    "INSERT INTO drop_replies "
                            + "(drop_id, user_id, parent_reply_id, audio_url, status) "
                            + "SELECT d.id, ?, NULL, ?, 'init'::drop_status "
                            + "FROM drops d "
                            + "WHERE d.id = ? "
                            + "AND d.status = 'approved'::drop_status "
                            + "AND d.expires_at > CURRENT_TIMESTAMP "
                            + "RETURNING " + REPLY_COLUMNS,
                    (resultSet, rowNum) -> mapReply(resultSet),
                    userId,
                    audioUrl,
                    dropId
            ).stream().findFirst().orElse(null);
        }

        return jdbcTemplate.query(
                "INSERT INTO drop_replies "
                        + "(drop_id, user_id, parent_reply_id, audio_url, status) "
                        + "SELECT p.drop_id, ?, p.id, ?, 'init'::drop_status "
                        + "FROM drop_replies p "
                        + "JOIN drops d ON d.id = p.drop_id "
                        + "WHERE p.id = ? "
                        + "AND p.drop_id = ? "
                        + "AND p.parent_reply_id IS NULL "
                        + "AND p.status = 'approved'::drop_status "
                        + "AND p.deleted_at IS NULL "
                        + "AND d.status = 'approved'::drop_status "
                        + "AND d.expires_at > CURRENT_TIMESTAMP "
                        + "RETURNING " + REPLY_COLUMNS,
                (resultSet, rowNum) -> mapReply(resultSet),
                userId,
                audioUrl,
                parentReplyId,
                dropId
        ).stream().findFirst().orElse(null);
    }

    public boolean markUploadCompleted(Long userId, Long replyId) {
        int updatedCount = jdbcTemplate.update(
                "UPDATE drop_replies r "
                        + "SET status = 'pending'::drop_status, updated_at = CURRENT_TIMESTAMP "
                        + "WHERE r.id = ? "
                        + "AND r.user_id = ? "
                        + "AND r.status IN ('init'::drop_status, 'pending'::drop_status) "
                        + "AND r.deleted_at IS NULL "
                        + "AND EXISTS ("
                        + "SELECT 1 FROM drops d "
                        + "WHERE d.id = r.drop_id "
                        + "AND d.status = 'approved'::drop_status "
                        + "AND d.expires_at > CURRENT_TIMESTAMP"
                        + ") "
                        + "AND (r.parent_reply_id IS NULL OR EXISTS ("
                        + "SELECT 1 FROM drop_replies parent "
                        + "WHERE parent.id = r.parent_reply_id "
                        + "AND parent.parent_reply_id IS NULL "
                        + "AND parent.status = 'approved'::drop_status "
                        + "AND parent.deleted_at IS NULL"
                        + "))",
                replyId,
                userId
        );
        return updatedCount > 0;
    }

    public DropReply findPendingReply(Long replyId) {
        return jdbcTemplate.query(
                "SELECT " + prefixedColumns("r") + " "
                        + "FROM drop_replies r "
                        + "JOIN drops d ON d.id = r.drop_id "
                        + "WHERE r.id = ? "
                        + "AND r.status = 'pending'::drop_status "
                        + "AND r.deleted_at IS NULL "
                        + "AND d.status = 'approved'::drop_status "
                        + "AND d.expires_at > CURRENT_TIMESTAMP "
                        + "AND (r.parent_reply_id IS NULL OR EXISTS ("
                        + "SELECT 1 FROM drop_replies parent "
                        + "WHERE parent.id = r.parent_reply_id "
                        + "AND parent.parent_reply_id IS NULL "
                        + "AND parent.status = 'approved'::drop_status "
                        + "AND parent.deleted_at IS NULL"
                        + "))",
                (resultSet, rowNum) -> mapReply(resultSet),
                replyId
        ).stream().findFirst().orElse(null);
    }

    public Boolean findModerationDecision(Long replyId) {
        List<String> statuses = jdbcTemplate.query(
                "SELECT status::text FROM drop_replies "
                        + "WHERE id = ? AND deleted_at IS NULL",
                (resultSet, rowNum) -> resultSet.getString("status"),
                replyId
        );
        if (statuses.isEmpty()) {
            return null;
        }
        if ("approved".equals(statuses.get(0))) {
            return true;
        }
        if ("rejected".equals(statuses.get(0))) {
            return false;
        }
        return null;
    }

    public boolean isOwnedAndAvailable(Long userId, Long replyId) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS("
                        + "SELECT 1 FROM drop_replies r "
                        + "JOIN drops d ON d.id = r.drop_id "
                        + "WHERE r.id = ? "
                        + "AND r.user_id = ? "
                        + "AND r.deleted_at IS NULL "
                        + "AND d.expires_at > CURRENT_TIMESTAMP"
                        + ")",
                Boolean.class,
                replyId,
                userId
        );
        return Boolean.TRUE.equals(exists);
    }

    public boolean isDropAvailable(Long dropId) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS("
                        + "SELECT 1 FROM drops "
                        + "WHERE id = ? "
                        + "AND status = 'approved'::drop_status "
                        + "AND expires_at > CURRENT_TIMESTAMP"
                        + ")",
                Boolean.class,
                dropId
        );
        return Boolean.TRUE.equals(exists);
    }

    public boolean isTopLevelReplyAvailable(Long parentReplyId) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS("
                        + "SELECT 1 FROM drop_replies r "
                        + "JOIN drops d ON d.id = r.drop_id "
                        + "WHERE r.id = ? "
                        + "AND r.parent_reply_id IS NULL "
                        + "AND r.status = 'approved'::drop_status "
                        + "AND r.deleted_at IS NULL "
                        + "AND d.status = 'approved'::drop_status "
                        + "AND d.expires_at > CURRENT_TIMESTAMP"
                        + ")",
                Boolean.class,
                parentReplyId
        );
        return Boolean.TRUE.equals(exists);
    }

    public List<ReplyListItem> findTopLevelReplies(
            Long userId,
            Long dropId,
            int limit,
            long offset
    ) {
        return jdbcTemplate.query(
                "SELECT " + REPLY_LIST_COLUMNS + " "
                        + "FROM drop_replies r "
                        + "JOIN drops d ON d.id = r.drop_id "
                        + "LEFT JOIN drop_reply_reactions reaction "
                        + "ON reaction.reply_id = r.id AND reaction.user_id = ? "
                        + "WHERE r.drop_id = ? "
                        + "AND r.parent_reply_id IS NULL "
                        + "AND r.status = 'approved'::drop_status "
                        + "AND r.deleted_at IS NULL "
                        + "AND d.status = 'approved'::drop_status "
                        + "AND d.expires_at > CURRENT_TIMESTAMP "
                        + "ORDER BY r.created_at DESC, r.id DESC "
                        + " LIMIT ? OFFSET ?",
                (resultSet, rowNum) -> mapReplyListItem(resultSet),
                userId,
                dropId,
                limit,
                offset
        );
    }

    public long countTopLevelReplies(Long dropId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) "
                        + "FROM drop_replies r "
                        + "JOIN drops d ON d.id = r.drop_id "
                        + "WHERE r.drop_id = ? "
                        + "AND r.parent_reply_id IS NULL "
                        + "AND r.status = 'approved'::drop_status "
                        + "AND r.deleted_at IS NULL "
                        + "AND d.status = 'approved'::drop_status "
                        + "AND d.expires_at > CURRENT_TIMESTAMP",
                Long.class,
                dropId
        );
        return count == null ? 0 : count;
    }

    public List<ReplyListItem> findChildReplies(
            Long userId,
            Long parentReplyId,
            int limit,
            long offset
    ) {
        return jdbcTemplate.query(
                "SELECT " + REPLY_LIST_COLUMNS + " "
                        + "FROM drop_replies r "
                        + "JOIN drops d ON d.id = r.drop_id "
                        + "LEFT JOIN drop_reply_reactions reaction "
                        + "ON reaction.reply_id = r.id AND reaction.user_id = ? "
                        + "WHERE r.parent_reply_id = ? "
                        + "AND r.status = 'approved'::drop_status "
                        + "AND r.deleted_at IS NULL "
                        + "AND d.status = 'approved'::drop_status "
                        + "AND d.expires_at > CURRENT_TIMESTAMP "
                        + "ORDER BY r.created_at ASC, r.id ASC "
                        + "LIMIT ? OFFSET ?",
                (resultSet, rowNum) -> mapReplyListItem(resultSet),
                userId,
                parentReplyId,
                limit,
                offset
        );
    }

    public long countChildReplies(Long parentReplyId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) "
                        + "FROM drop_replies r "
                        + "JOIN drops d ON d.id = r.drop_id "
                        + "WHERE r.parent_reply_id = ? "
                        + "AND r.status = 'approved'::drop_status "
                        + "AND r.deleted_at IS NULL "
                        + "AND d.status = 'approved'::drop_status "
                        + "AND d.expires_at > CURRENT_TIMESTAMP",
                Long.class,
                parentReplyId
        );
        return count == null ? 0 : count;
    }

    public boolean lockAvailableReplyForReaction(Long replyId) {
        return !jdbcTemplate.query(
                "SELECT r.id "
                        + "FROM drop_replies r "
                        + "JOIN drops d ON d.id = r.drop_id "
                        + "WHERE r.id = ? "
                        + "AND r.status = 'approved'::drop_status "
                        + "AND r.deleted_at IS NULL "
                        + "AND d.status = 'approved'::drop_status "
                        + "AND d.expires_at > CURRENT_TIMESTAMP "
                        + "FOR UPDATE OF r",
                (resultSet, rowNum) -> resultSet.getLong("id"),
                replyId
        ).isEmpty();
    }

    public ReplyReactionResponse findReactionState(Long userId, Long replyId) {
        return jdbcTemplate.query(
                "SELECT r.id, r.like_count, r.dislike_count, "
                        + "reaction.reaction::text AS reaction "
                        + "FROM drop_replies r "
                        + "LEFT JOIN drop_reply_reactions reaction "
                        + "ON reaction.reply_id = r.id AND reaction.user_id = ? "
                        + "WHERE r.id = ?",
                (resultSet, rowNum) -> new ReplyReactionResponse(
                        resultSet.getLong("id"),
                        resultSet.getLong("like_count"),
                        resultSet.getLong("dislike_count"),
                        resultSet.getString("reaction")
                ),
                userId,
                replyId
        ).stream().findFirst().orElse(null);
    }

    public int upsertReaction(Long userId, Long replyId, String reaction) {
        return jdbcTemplate.update(
                "INSERT INTO drop_reply_reactions (reply_id, user_id, reaction) "
                        + "VALUES (?, ?, ?::drop_reply_reaction_type) "
                        + "ON CONFLICT (reply_id, user_id) DO UPDATE "
                        + "SET reaction = EXCLUDED.reaction, "
                        + "updated_at = CURRENT_TIMESTAMP",
                replyId,
                userId,
                reaction
        );
    }

    public int deleteReaction(Long userId, Long replyId) {
        return jdbcTemplate.update(
                "DELETE FROM drop_reply_reactions "
                        + "WHERE reply_id = ? AND user_id = ?",
                replyId,
                userId
        );
    }

    public ReplyReactionResponse updateReactionCounts(
            Long replyId,
            int likeDelta,
            int dislikeDelta,
            String reaction
    ) {
        return jdbcTemplate.query(
                "UPDATE drop_replies "
                        + "SET like_count = like_count + ?, "
                        + "dislike_count = dislike_count + ? "
                        + "WHERE id = ? "
                        + "AND like_count + ? >= 0 "
                        + "AND dislike_count + ? >= 0 "
                        + "RETURNING id, like_count, dislike_count",
                (resultSet, rowNum) -> new ReplyReactionResponse(
                        resultSet.getLong("id"),
                        resultSet.getLong("like_count"),
                        resultSet.getLong("dislike_count"),
                        reaction
                ),
                likeDelta,
                dislikeDelta,
                replyId,
                likeDelta,
                dislikeDelta
        ).stream().findFirst().orElse(null);
    }

    public DropReply findOwnedReplyForUpdate(Long userId, Long replyId) {
        return jdbcTemplate.query(
                "SELECT " + REPLY_COLUMNS + " "
                        + "FROM drop_replies "
                        + "WHERE id = ? "
                        + "AND user_id = ? "
                        + "AND deleted_at IS NULL "
                        + "FOR UPDATE",
                (resultSet, rowNum) -> mapReply(resultSet),
                replyId,
                userId
        ).stream().findFirst().orElse(null);
    }

    public boolean hasNonDeletedChildReplies(Long replyId) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS("
                        + "SELECT 1 FROM drop_replies "
                        + "WHERE parent_reply_id = ? "
                        + "AND deleted_at IS NULL"
                        + ")",
                Boolean.class,
                replyId
        );
        return Boolean.TRUE.equals(exists);
    }

    public int softDeleteReply(Long userId, Long replyId) {
        return jdbcTemplate.update(
                "UPDATE drop_replies "
                        + "SET deleted_at = CURRENT_TIMESTAMP, "
                        + "updated_at = CURRENT_TIMESTAMP "
                        + "WHERE id = ? "
                        + "AND user_id = ? "
                        + "AND deleted_at IS NULL",
                replyId,
                userId
        );
    }

    public DropReply updateModerationStatus(Long replyId, boolean approved) {
        String targetStatus = approved ? "approved" : "rejected";
        return jdbcTemplate.query(
                "UPDATE drop_replies r "
                        + "SET status = ?::drop_status, updated_at = CURRENT_TIMESTAMP "
                        + "WHERE r.id = ? "
                        + "AND r.status = 'pending'::drop_status "
                        + "AND r.deleted_at IS NULL "
                        + "AND EXISTS ("
                        + "SELECT 1 FROM drops d "
                        + "WHERE d.id = r.drop_id "
                        + "AND d.status = 'approved'::drop_status "
                        + "AND d.expires_at > CURRENT_TIMESTAMP"
                        + ") "
                        + "RETURNING " + REPLY_COLUMNS,
                (resultSet, rowNum) -> mapReply(resultSet),
                targetStatus,
                replyId
        ).stream().findFirst().orElse(null);
    }

    public int incrementDropReplyCount(Long dropId) {
        return jdbcTemplate.update(
                "UPDATE drops "
                        + "SET reply_count = reply_count + 1 "
                        + "WHERE id = ? "
                        + "AND status = 'approved'::drop_status "
                        + "AND expires_at > CURRENT_TIMESTAMP",
                dropId
        );
    }

    public int incrementChildReplyCount(Long parentReplyId) {
        return jdbcTemplate.update(
                "UPDATE drop_replies "
                        + "SET child_reply_count = child_reply_count + 1, "
                        + "updated_at = CURRENT_TIMESTAMP "
                        + "WHERE id = ? "
                        + "AND parent_reply_id IS NULL "
                        + "AND status = 'approved'::drop_status "
                        + "AND deleted_at IS NULL",
                parentReplyId
        );
    }

    public int decrementDropReplyCount(Long dropId) {
        return jdbcTemplate.update(
                "UPDATE drops "
                        + "SET reply_count = reply_count - 1 "
                        + "WHERE id = ? "
                        + "AND reply_count > 0",
                dropId
        );
    }

    public int decrementChildReplyCount(Long parentReplyId) {
        return jdbcTemplate.update(
                "UPDATE drop_replies "
                        + "SET child_reply_count = child_reply_count - 1, "
                        + "updated_at = CURRENT_TIMESTAMP "
                        + "WHERE id = ? "
                        + "AND parent_reply_id IS NULL "
                        + "AND child_reply_count > 0",
                parentReplyId
        );
    }

    private String prefixedColumns(String alias) {
        return alias + ".id, "
                + alias + ".drop_id, "
                + alias + ".user_id, "
                + alias + ".parent_reply_id, "
                + alias + ".audio_url, "
                + alias + ".status::text AS status, "
                + alias + ".like_count, "
                + alias + ".dislike_count, "
                + alias + ".child_reply_count, "
                + alias + ".created_at, "
                + alias + ".updated_at, "
                + alias + ".deleted_at";
    }

    private ReplyListItem mapReplyListItem(ResultSet resultSet) throws SQLException {
        return new ReplyListItem(
                resultSet.getLong("id"),
                resultSet.getLong("user_id"),
                null,
                null,
                resultSet.getString("audio_url"),
                resultSet.getLong("like_count"),
                resultSet.getLong("dislike_count"),
                resultSet.getString("reaction"),
                resultSet.getBoolean("drop_author"),
                resultSet.getLong("child_reply_count"),
                getInstant(resultSet, "created_at")
        );
    }

    private DropReply mapReply(ResultSet resultSet) throws SQLException {
        return new DropReply(
                resultSet.getLong("id"),
                resultSet.getLong("drop_id"),
                resultSet.getLong("user_id"),
                getNullableLong(resultSet, "parent_reply_id"),
                resultSet.getString("audio_url"),
                resultSet.getString("status"),
                resultSet.getLong("like_count"),
                resultSet.getLong("dislike_count"),
                resultSet.getLong("child_reply_count"),
                getInstant(resultSet, "created_at"),
                getInstant(resultSet, "updated_at"),
                getInstant(resultSet, "deleted_at")
        );
    }

    private Long getNullableLong(ResultSet resultSet, String columnName) throws SQLException {
        long value = resultSet.getLong(columnName);
        return resultSet.wasNull() ? null : value;
    }

    private java.time.Instant getInstant(ResultSet resultSet, String columnName)
            throws SQLException {
        OffsetDateTime value = resultSet.getObject(columnName, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }
}
