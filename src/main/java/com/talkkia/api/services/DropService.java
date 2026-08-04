package com.talkkia.api.services;

import com.talkkia.api.dto.drop.DeleteDropRequest;
import com.talkkia.api.dto.drop.DropListPageResponse;
import com.talkkia.api.dto.drop.LatestDropsPageResponse;
import com.talkkia.api.entity.Drop;

public interface DropService {

    Drop createInitialDrop(Long userId, Long cityId, String audioUrl);

    boolean markUploadCompleted(Long userId, Long dropId);

    Drop findPendingDrop(Long dropId);

    boolean recordModerationResult(Long dropId, boolean approved);

    boolean isOwnedAndAvailable(Long userId, Long dropId);

    boolean deleteDrop(Long userId, DeleteDropRequest request);

    Boolean findModerationDecision(Long dropId);

    DropListPageResponse findLatestAvailableDrops(
            Long userId,
            Long cityId,
            String cityCode,
            boolean includeCityOptions,
            Integer page
    );

    LatestDropsPageResponse findLatestUserDrops(Long userId, Integer page);
}
