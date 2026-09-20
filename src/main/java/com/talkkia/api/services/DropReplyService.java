package com.talkkia.api.services;

import com.talkkia.api.dto.reply.ChildReplyListPageResponse;
import com.talkkia.api.dto.reply.CreateReplyUploadRequest;
import com.talkkia.api.dto.reply.CreateReplyUploadResponse;
import com.talkkia.api.dto.reply.DeleteReplyRequest;
import com.talkkia.api.dto.reply.ReplyListPageResponse;
import com.talkkia.api.dto.reply.ReplyReactionRequest;
import com.talkkia.api.dto.reply.ReplyReactionResponse;
import com.talkkia.api.entity.DropReply;

public interface DropReplyService {

    CreateReplyUploadResponse createUploadUrl(
            Long userId,
            CreateReplyUploadRequest request
    );

    boolean markUploadCompleted(Long userId, Long replyId);

    DropReply findPendingReply(Long replyId);

    Boolean findModerationDecision(Long replyId);

    boolean isOwnedAndAvailable(Long userId, Long replyId);

    ReplyListPageResponse findTopLevelReplies(
            Long userId,
            Long dropId,
            Integer page,
            Integer size
    );

    ChildReplyListPageResponse findChildReplies(
            Long userId,
            Long parentReplyId,
            Integer page,
            Integer size
    );

    ReplyReactionResponse setReaction(
            Long userId,
            ReplyReactionRequest request
    );

    boolean deleteReply(Long userId, DeleteReplyRequest request);

    boolean recordModerationResult(Long replyId, boolean approved);
}
