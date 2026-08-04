package com.talkkia.api.services;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.talkkia.api.dto.reply.ChildReplyListPageResponse;
import com.talkkia.api.dto.reply.CreateReplyUploadRequest;
import com.talkkia.api.dto.reply.CreateReplyUploadResponse;
import com.talkkia.api.dto.reply.DeleteReplyRequest;
import com.talkkia.api.dto.reply.ReplyListItem;
import com.talkkia.api.dto.reply.ReplyListPageResponse;
import com.talkkia.api.dto.reply.ReplyReactionRequest;
import com.talkkia.api.dto.reply.ReplyReactionResponse;
import com.talkkia.api.entity.DropReply;
import com.talkkia.api.entity.User;
import com.talkkia.api.enums.DropReplySort;
import com.talkkia.api.repositories.DropReplyRepository;
import com.talkkia.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class DropReplyServiceImpl implements DropReplyService {

    private static final int DEFAULT_TOP_LEVEL_PAGE_SIZE = 10;
    private static final int DEFAULT_CHILD_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    @Autowired
    private DropReplyRepository dropReplyRepository;

    @Autowired
    private R2UploadService uploadService;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public CreateReplyUploadResponse createUploadUrl(
            Long userId,
            CreateReplyUploadRequest request
    ) {
        validateCreateRequest(userId, request);

        CreateReplyUploadResponse uploadResponse =
                uploadService.createReplyUploadUrl(userId.toString(), request);
        DropReply reply = dropReplyRepository.createInitialReply(
                userId,
                request.getDropId(),
                request.getParentReplyId(),
                uploadResponse.getObjectKey()
        );
        if (reply == null) {
            throw new NoSuchElementException(
                    "Drop or parent reply not found, expired, or unavailable"
            );
        }

        uploadResponse.setReplyId(reply.getId());
        return uploadResponse;
    }

    @Override
    public boolean markUploadCompleted(Long userId, Long replyId) {
        validateIds(userId, replyId);
        return dropReplyRepository.markUploadCompleted(userId, replyId);
    }

    @Override
    public DropReply findPendingReply(Long replyId) {
        validateReplyId(replyId);
        return dropReplyRepository.findPendingReply(replyId);
    }

    @Override
    public Boolean findModerationDecision(Long replyId) {
        validateReplyId(replyId);
        return dropReplyRepository.findModerationDecision(replyId);
    }

    @Override
    public boolean isOwnedAndAvailable(Long userId, Long replyId) {
        validateIds(userId, replyId);
        return dropReplyRepository.isOwnedAndAvailable(userId, replyId);
    }

    @Override
    public ReplyListPageResponse findTopLevelReplies(
            Long userId,
            Long dropId,
            String sort,
            Integer page,
            Integer size
    ) {
        validateUserId(userId);
        if (dropId == null || dropId <= 0) {
            throw new IllegalArgumentException("Drop ID is required");
        }
        if (!dropReplyRepository.isDropAvailable(dropId)) {
            throw new NoSuchElementException("Drop not found, expired, or unavailable");
        }

        int resolvedPage = resolvePage(page);
        int resolvedSize = resolveSize(size, DEFAULT_TOP_LEVEL_PAGE_SIZE);
        DropReplySort resolvedSort = DropReplySort.fromValue(sort);
        long totalElements = dropReplyRepository.countTopLevelReplies(dropId);
        List<ReplyListItem> content = dropReplyRepository.findTopLevelReplies(
                userId,
                dropId,
                resolvedSort,
                resolvedSize,
                (long) resolvedPage * resolvedSize
        );
        enrichUserProfiles(content);
        long totalPages = calculateTotalPages(totalElements, resolvedSize);

        return new ReplyListPageResponse(
                dropId,
                resolvedSort.getValue(),
                content,
                resolvedPage,
                resolvedSize,
                totalElements,
                totalPages,
                resolvedPage + 1L < totalPages
        );
    }

    @Override
    public ChildReplyListPageResponse findChildReplies(
            Long userId,
            Long parentReplyId,
            Integer page,
            Integer size
    ) {
        validateUserId(userId);
        if (parentReplyId == null || parentReplyId <= 0) {
            throw new IllegalArgumentException("Parent reply ID is required");
        }
        if (!dropReplyRepository.isTopLevelReplyAvailable(parentReplyId)) {
            throw new NoSuchElementException(
                    "Parent reply not found, expired, or unavailable"
            );
        }

        int resolvedPage = resolvePage(page);
        int resolvedSize = resolveSize(size, DEFAULT_CHILD_PAGE_SIZE);
        long totalElements = dropReplyRepository.countChildReplies(parentReplyId);
        List<ReplyListItem> content = dropReplyRepository.findChildReplies(
                userId,
                parentReplyId,
                resolvedSize,
                (long) resolvedPage * resolvedSize
        );
        enrichUserProfiles(content);
        long totalPages = calculateTotalPages(totalElements, resolvedSize);

        return new ChildReplyListPageResponse(
                parentReplyId,
                content,
                resolvedPage,
                resolvedSize,
                totalElements,
                totalPages,
                resolvedPage + 1L < totalPages
        );
    }

    @Override
    @Transactional
    public ReplyReactionResponse setReaction(
            Long userId,
            ReplyReactionRequest request
    ) {
        validateUserId(userId);
        if (request == null) {
            throw new IllegalArgumentException("Reply reaction request is required");
        }
        validateReplyId(request.getReplyId());

        String reaction = normalizeReaction(request.getReaction());
        if (!dropReplyRepository.lockAvailableReplyForReaction(
                request.getReplyId())) {
            throw new NoSuchElementException(
                    "Reply not found, expired, or unavailable"
            );
        }
        ReplyReactionResponse currentState =
                dropReplyRepository.findReactionState(
                        userId,
                        request.getReplyId()
                );
        if (currentState == null) {
            throw new IllegalStateException("Failed to read reply reaction state");
        }

        String currentReaction = currentState.getReaction();
        if (Objects.equals(currentReaction, reaction)) {
            return currentState;
        }

        int reactionUpdates;
        if (reaction == null) {
            reactionUpdates = dropReplyRepository.deleteReaction(
                    userId,
                    request.getReplyId()
            );
        } else {
            reactionUpdates = dropReplyRepository.upsertReaction(
                    userId,
                    request.getReplyId(),
                    reaction
            );
        }
        if (reactionUpdates != 1) {
            throw new IllegalStateException("Failed to update reply reaction");
        }

        int likeDelta = reactionDelta(currentReaction, reaction, "like");
        int dislikeDelta = reactionDelta(currentReaction, reaction, "dislike");
        ReplyReactionResponse updatedState =
                dropReplyRepository.updateReactionCounts(
                        request.getReplyId(),
                        likeDelta,
                        dislikeDelta,
                        reaction
                );
        if (updatedState == null) {
            throw new IllegalStateException("Failed to update reply reaction counts");
        }
        return updatedState;
    }

    @Override
    @Transactional
    public boolean deleteReply(Long userId, DeleteReplyRequest request) {
        validateUserId(userId);
        if (request == null) {
            throw new IllegalArgumentException("Delete reply request is required");
        }
        validateReplyId(request.getReplyId());

        DropReply reply = dropReplyRepository.findOwnedReplyForUpdate(
                userId,
                request.getReplyId()
        );
        if (reply == null) {
            throw new NoSuchElementException(
                    "Reply not found or not owned by user"
            );
        }
        if (reply.getParentReplyId() == null
                && dropReplyRepository.hasNonDeletedChildReplies(reply.getId())) {
            throw new IllegalStateException(
                    "Reply with child replies cannot be deleted"
            );
        }
        if (dropReplyRepository.softDeleteReply(
                userId,
                request.getReplyId()) != 1) {
            throw new IllegalStateException("Failed to delete reply");
        }

        if (!"approved".equals(reply.getStatus())) {
            return true;
        }
        if (dropReplyRepository.decrementDropReplyCount(reply.getDropId()) != 1) {
            throw new IllegalStateException("Failed to update drop reply count");
        }
        if (reply.getParentReplyId() != null
                && dropReplyRepository.decrementChildReplyCount(
                        reply.getParentReplyId()) != 1) {
            throw new IllegalStateException("Failed to update child reply count");
        }
        return true;
    }

    @Override
    @Transactional
    public boolean recordModerationResult(Long replyId, boolean approved) {
        validateReplyId(replyId);
        DropReply updatedReply =
                dropReplyRepository.updateModerationStatus(replyId, approved);
        if (updatedReply == null) {
            return false;
        }
        if (!approved) {
            return true;
        }

        if (dropReplyRepository.incrementDropReplyCount(updatedReply.getDropId()) != 1) {
            throw new IllegalStateException("Failed to update drop reply count");
        }
        if (updatedReply.getParentReplyId() != null
                && dropReplyRepository.incrementChildReplyCount(
                        updatedReply.getParentReplyId()) != 1) {
            throw new IllegalStateException("Failed to update child reply count");
        }
        return true;
    }

    private String normalizeReaction(String reaction) {
        if (reaction == null) {
            return null;
        }

        String normalizedReaction = reaction.trim().toLowerCase(Locale.ROOT);
        if (!"like".equals(normalizedReaction)
                && !"dislike".equals(normalizedReaction)) {
            throw new IllegalArgumentException(
                    "Reaction must be like, dislike, or null"
            );
        }
        return normalizedReaction;
    }

    private int reactionDelta(
            String currentReaction,
            String targetReaction,
            String countedReaction
    ) {
        int previousValue = countedReaction.equals(currentReaction) ? 1 : 0;
        int targetValue = countedReaction.equals(targetReaction) ? 1 : 0;
        return targetValue - previousValue;
    }

    private void validateCreateRequest(Long userId, CreateReplyUploadRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Reply upload request is required");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (request.getDropId() == null || request.getDropId() <= 0) {
            throw new IllegalArgumentException("Drop ID is required");
        }
        if (request.getParentReplyId() != null && request.getParentReplyId() <= 0) {
            throw new IllegalArgumentException("Parent reply ID must be positive");
        }
        if (!StringUtils.hasText(request.getExtension())
                || !StringUtils.hasText(request.getContentType())) {
            throw new IllegalArgumentException(
                    "Audio extension and content type are required"
            );
        }
    }

    private void validateIds(Long userId, Long replyId) {
        validateUserId(userId);
        validateReplyId(replyId);
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID is required");
        }
    }

    private void validateReplyId(Long replyId) {
        if (replyId == null || replyId <= 0) {
            throw new IllegalArgumentException("Reply ID is required");
        }
    }

    private int resolvePage(Integer page) {
        int resolvedPage = page == null ? 0 : page;
        if (resolvedPage < 0) {
            throw new IllegalArgumentException("Page must not be negative");
        }
        return resolvedPage;
    }

    private int resolveSize(Integer size, int defaultSize) {
        int resolvedSize = size == null ? defaultSize : size;
        if (resolvedSize <= 0 || resolvedSize > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and " + MAX_PAGE_SIZE
            );
        }
        return resolvedSize;
    }

    private long calculateTotalPages(long totalElements, int size) {
        long totalPages = totalElements / size;
        return totalElements % size == 0 ? totalPages : totalPages + 1;
    }

    private void enrichUserProfiles(List<ReplyListItem> content) {
        List<Long> userIds = content.stream()
                .map(ReplyListItem::getUserId)
                .distinct()
                .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return;
        }

        Map<Long, User> usersById = userRepository.findDropListUsers(userIds).stream()
                .collect(Collectors.toMap(
                        User::getId,
                        Function.identity(),
                        (first, second) -> first
                ));
        content.forEach(item -> {
            User user = usersById.get(item.getUserId());
            if (user != null) {
                item.setUsername(user.getName());
                item.setAvatarUrl(user.getPicture());
            }
        });
    }
}
