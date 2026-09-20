package com.talkkia.api.services;

import com.talkkia.api.entity.Drop;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class DropModerationService {

    @Autowired
    private DropService dropService;

    @Autowired
    private WhisperService whisperService;

    @Autowired
    private R2UploadService r2UploadService;

    @Autowired
    private KeywordModerationService keywordModerationService;

    public Boolean moderate(Long dropId) {
        Drop drop = dropService.findPendingDrop(dropId);
        if (drop == null) {
            Boolean decision = dropService.findModerationDecision(dropId);
            if (decision != null) {
                return decision;
            }
            log.info("Skip moderation for unavailable drop {}", dropId);
            return null;
        }

        boolean approved = moderateAudioContent(drop.getAudioUrl());
        boolean updated = dropService.recordModerationResult(dropId, approved);
        if (updated) {
            log.info("Moderation completed for drop {} with status {}", dropId,
                    approved ? "approved" : "rejected");
            return approved;
        }

        return null;
    }

    public boolean moderateAudioContent(String objectKey) {
        String transcript = transcribeAudio(objectKey);
        if (!StringUtils.hasText(transcript)) {
            return false;
        }
        return !keywordModerationService.containsBlockedKeyword(transcript);
    }

    private String transcribeAudio(String objectKey) {
        String downloadUrl = r2UploadService.createDownloadUrl(objectKey);
        return whisperService.transcribeFromUrl(downloadUrl);
    }

}
