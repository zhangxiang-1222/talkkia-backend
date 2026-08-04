package com.talkkia.api.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DropModerationConsumer {

    @Autowired
    private DropModerationService moderationService;

    @Autowired
    private DropModerationResultCache resultCache;

    @JmsListener(destination = "${drop.moderation.queue}")
    public void consume(String message) {
        Long dropId;
        try {
            dropId = Long.parseLong(message);
        } catch (NumberFormatException e) {
            log.error("Discard invalid drop moderation message: {}", message);
            return;
        }

        try {
            Boolean approved = moderationService.moderate(dropId);
            if (approved != null) {
                resultCache.record(dropId, approved);
            }
        } catch (RuntimeException e) {
            log.error("Moderation failed for drop {}", dropId, e);
            throw e;
        }
    }
}
