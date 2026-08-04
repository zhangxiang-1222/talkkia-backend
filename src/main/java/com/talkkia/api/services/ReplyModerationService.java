package com.talkkia.api.services;

import com.talkkia.api.entity.DropReply;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReplyModerationService {

    @Autowired
    private DropReplyService dropReplyService;

    @Autowired
    private DropModerationService dropModerationService;

    public Boolean moderate(Long replyId) {
        DropReply reply = dropReplyService.findPendingReply(replyId);
        if (reply == null) {
            Boolean decision = dropReplyService.findModerationDecision(replyId);
            if (decision != null) {
                return decision;
            }
            log.info("Skip moderation for unavailable reply {}", replyId);
            return null;
        }

        boolean approved =
                dropModerationService.moderateAudioContent(reply.getAudioUrl());
        boolean updated =
                dropReplyService.recordModerationResult(replyId, approved);
        if (!updated) {
            return null;
        }

        log.info("Moderation completed for reply {} with status {}", replyId,
                approved ? "approved" : "rejected");
        return approved;
    }
}
