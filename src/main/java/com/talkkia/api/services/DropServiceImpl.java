package com.talkkia.api.services;

import com.talkkia.api.dto.drop.DeleteDropRequest;
import com.talkkia.api.dto.drop.DropListItem;
import com.talkkia.api.dto.drop.DropCityOption;
import com.talkkia.api.dto.drop.DropListPageResponse;
import com.talkkia.api.dto.drop.DropReactionRequest;
import com.talkkia.api.dto.drop.DropReactionResponse;
import com.talkkia.api.dto.drop.DropReactionState;
import com.talkkia.api.dto.drop.LatestDropsPageResponse;
import com.talkkia.api.entity.City;
import com.talkkia.api.entity.Drop;
import com.talkkia.api.entity.User;
import com.talkkia.api.repositories.CityRepository;
import com.talkkia.api.repositories.DropRepository;
import com.talkkia.api.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DropServiceImpl implements DropService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int CITY_OPTION_LIMIT = 5;

    private final DropRepository dropRepository;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;

    public DropServiceImpl(
            DropRepository dropRepository,
            CityRepository cityRepository,
            UserRepository userRepository
    ) {
        this.dropRepository = dropRepository;
        this.cityRepository = cityRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Drop createInitialDrop(Long userId, Long cityId, String audioUrl) {
        if (userId == null || userId <= 0 || cityId == null || cityId <= 0
                || !StringUtils.hasText(audioUrl)) {
            throw new IllegalArgumentException("User ID, city ID, and audio URL are required");
        }

        return dropRepository.createInitialDrop(userId, cityId, audioUrl);
    }

    @Override
    public boolean markUploadCompleted(Long userId, Long dropId) {
        if (userId == null || userId <= 0 || dropId == null || dropId <= 0) {
            throw new IllegalArgumentException("User ID and drop ID are required");
        }

        return dropRepository.markUploadCompleted(userId, dropId);
    }

    @Override
    public Drop findPendingDrop(Long dropId) {
        if (dropId == null || dropId <= 0) {
            throw new IllegalArgumentException("Drop ID is required");
        }

        return dropRepository.findPendingDrop(dropId);
    }

    @Override
    public boolean recordModerationResult(Long dropId, boolean approved) {
        if (dropId == null || dropId <= 0) {
            throw new IllegalArgumentException("Drop ID is required");
        }

        return dropRepository.recordModerationResult(dropId, approved);
    }

    @Override
    public boolean isOwnedAndAvailable(Long userId, Long dropId) {
        if (userId == null || userId <= 0 || dropId == null || dropId <= 0) {
            throw new IllegalArgumentException("User ID and drop ID are required");
        }

        return dropRepository.isOwnedAndAvailable(userId, dropId);
    }

    @Override
    public Long incrementListen(Long dropId) {
        if (dropId == null || dropId <= 0) {
            throw new IllegalArgumentException("Drop ID is required");
        }

        Long listen = dropRepository.incrementListen(dropId);
        if (listen == null) {
            throw new NoSuchElementException("Drop not found, expired, or unavailable");
        }
        return listen;
    }

    @Override
    @Transactional
    public boolean deleteDrop(Long userId, DeleteDropRequest request) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("Delete drop request is required");
        }
        if (request.getDropId() == null || request.getDropId() <= 0) {
            throw new IllegalArgumentException("Drop ID is required");
        }

        if (dropRepository.deleteOwnedDrop(
                userId,
                request.getDropId()) != 1) {
            throw new NoSuchElementException(
                    "Drop not found or not owned by user"
            );
        }
        return true;
    }

    @Override
    public Boolean findModerationDecision(Long dropId) {
        if (dropId == null || dropId <= 0) {
            throw new IllegalArgumentException("Drop ID is required");
        }

        return dropRepository.findModerationDecision(dropId);
    }

    @Override
    public DropListPageResponse findLatestAvailableDrops(
            Long userId,
            Long cityId,
            String cityCode,
            boolean includeCityOptions,
            Integer page
    ) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID is required");
        }

        City selectedCity = resolveSelectedCity(cityId, cityCode);
        int resolvedPage = page == null ? 0 : page;
        if (resolvedPage < 0) {
            throw new IllegalArgumentException("Page must not be negative");
        }

        long totalElements = dropRepository.countLatestAvailableDrops(selectedCity.getId());
        long offset = (long) resolvedPage * DEFAULT_PAGE_SIZE;
        List<DropListItem> content =
                dropRepository.findLatestAvailableDrops(
                        userId,
                        selectedCity.getId(),
                        DEFAULT_PAGE_SIZE,
                        offset
                );
        enrichUserProfiles(content);
        long totalPages = totalElements / DEFAULT_PAGE_SIZE;
        if (totalElements % DEFAULT_PAGE_SIZE != 0) {
            totalPages++;
        }

        DropCityOption selectedCityOption = toCityOption(selectedCity);
        List<DropCityOption> cityOptions = null;
        if (includeCityOptions) {
            cityOptions = cityRepository.findCityOptions(
                            selectedCity.getId(),
                            CITY_OPTION_LIMIT
                    ).stream()
                    .map(this::toCityOption)
                    .collect(Collectors.toList());
        }

        return new DropListPageResponse(
                selectedCityOption,
                cityOptions,
                content,
                resolvedPage,
                DEFAULT_PAGE_SIZE,
                totalElements,
                totalPages,
                resolvedPage + 1L < totalPages
        );
    }

    private City resolveSelectedCity(Long cityId, String cityCode) {
        City selectedCity;
        if (cityId != null) {
            if (cityId <= 0) {
                throw new IllegalArgumentException("City ID must be positive");
            }
            selectedCity = cityRepository.findById(cityId);
        } else if (StringUtils.hasText(cityCode)) {
            selectedCity = cityRepository.findByCityCode(cityCode);
        } else {
            throw new IllegalArgumentException("City ID or city code is required");
        }

        if (selectedCity == null) {
            throw new NoSuchElementException("City not found");
        }
        return selectedCity;
    }

    private DropCityOption toCityOption(City city) {
        return new DropCityOption(
                city.getId(),
                city.getCityCode(),
                city.getCityName()
        );
    }

    private void enrichUserProfiles(List<DropListItem> content) {
        List<Long> userIds = content.stream()
                .map(DropListItem::getUserId)
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

    @Override
    @Transactional
    public DropReactionResponse setReaction(
            Long userId,
            DropReactionRequest request
    ) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("Drop reaction request is required");
        }
        if (request.getDropId() == null || request.getDropId() <= 0) {
            throw new IllegalArgumentException("Drop ID is required");
        }
        if (request.getEnabled() == null) {
            throw new IllegalArgumentException("Reaction enabled state is required");
        }

        String reaction = normalizeReaction(request.getReaction());
        if (!dropRepository.lockAvailableDropForReaction(request.getDropId())) {
            throw new NoSuchElementException("Drop not found, expired, or unavailable");
        }

        DropReactionState currentState = dropRepository.findReactionState(
                userId,
                request.getDropId()
        );
        boolean currentLiked = Boolean.TRUE.equals(currentState.getLiked());
        boolean currentLaughed = Boolean.TRUE.equals(currentState.getLaughed());
        boolean targetLiked = "like".equals(reaction)
                ? request.getEnabled() : currentLiked;
        boolean targetLaughed = "funny".equals(reaction)
                ? request.getEnabled() : currentLaughed;

        if (currentLiked == targetLiked && currentLaughed == targetLaughed) {
            return requireReactionSummary(
                    request.getDropId(),
                    targetLiked,
                    targetLaughed
            );
        }

        int stateUpdates;
        if (!targetLiked && !targetLaughed) {
            stateUpdates = dropRepository.deleteReaction(userId, request.getDropId());
        } else {
            stateUpdates = dropRepository.upsertReaction(
                    userId,
                    request.getDropId(),
                    targetLiked,
                    targetLaughed
            );
        }
        if (stateUpdates != 1) {
            throw new IllegalStateException("Failed to update drop reaction");
        }

        int likeDelta = reactionDelta(currentLiked, targetLiked);
        int funnyDelta = reactionDelta(currentLaughed, targetLaughed);
        DropReactionResponse response = dropRepository.updateReactionCounts(
                request.getDropId(),
                likeDelta,
                funnyDelta,
                targetLiked,
                targetLaughed
        );
        if (response == null) {
            throw new IllegalStateException("Failed to update drop reaction counts");
        }
        return response;
    }

    private String normalizeReaction(String reaction) {
        if (!StringUtils.hasText(reaction)) {
            throw new IllegalArgumentException("Reaction is required");
        }
        String normalizedReaction = reaction.trim().toLowerCase(Locale.ROOT);
        if (!"like".equals(normalizedReaction)
                && !"funny".equals(normalizedReaction)) {
            throw new IllegalArgumentException("Reaction must be like or funny");
        }
        return normalizedReaction;
    }

    private int reactionDelta(boolean currentValue, boolean targetValue) {
        return (targetValue ? 1 : 0) - (currentValue ? 1 : 0);
    }

    private DropReactionResponse requireReactionSummary(
            Long dropId,
            boolean liked,
            boolean laughed
    ) {
        DropReactionResponse response = dropRepository.findReactionSummary(
                dropId,
                liked,
                laughed
        );
        if (response == null) {
            throw new IllegalStateException("Failed to read drop reaction summary");
        }
        return response;
    }

    @Override
    public LatestDropsPageResponse findLatestUserDrops(Long userId, Integer page) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID is required");
        }

        int resolvedPage = page == null ? 0 : page;
        if (resolvedPage < 0) {
            throw new IllegalArgumentException("Page must not be negative");
        }

        long totalElements = dropRepository.countLatestUserDrops(userId);
        long offset = (long) resolvedPage * DEFAULT_PAGE_SIZE;
        List<Drop> content =
                dropRepository.findLatestUserDrops(userId, DEFAULT_PAGE_SIZE, offset);
        long totalPages = totalElements / DEFAULT_PAGE_SIZE;
        if (totalElements % DEFAULT_PAGE_SIZE != 0) {
            totalPages++;
        }

        return new LatestDropsPageResponse(
                content,
                resolvedPage,
                DEFAULT_PAGE_SIZE,
                totalElements,
                totalPages,
                resolvedPage + 1L < totalPages
        );
    }
}
