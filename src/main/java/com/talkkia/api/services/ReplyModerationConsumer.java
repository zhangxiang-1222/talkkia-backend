package com.talkkia.api.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReplyModerationConsumer {

    @Autowired
    private ReplyModerationService moderationService;

    @Autowired
    private ReplyModerationResultCache resultCache;

    @JmsListener(destination = "${reply.moderation.queue}")
    public void consume(String message) {
        Long replyId;
        try {
            replyId = Long.parseLong(message);
        } catch (NumberFormatException e) {
            log.error("Discard invalid reply moderation message: {}", message);
            return;
        }

        try {
            Boolean approved = moderationService.moderate(replyId);
            if (approved != null) {
                resultCache.record(replyId, approved);
            }
        } catch (RuntimeException e) {
            log.error("Moderation failed for reply {}", replyId, e);
            throw e;
        }
    }
}
