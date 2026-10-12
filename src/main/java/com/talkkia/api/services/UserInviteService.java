package com.talkkia.api.services;

import com.talkkia.api.config.DateTimeConverter;
import com.talkkia.api.entity.InviteAttribution;
import com.talkkia.api.entity.User;
import com.talkkia.api.model.SocialLoginRequest;
import com.talkkia.api.repositories.InviteAttributionRepository;
import com.talkkia.api.repositories.UserRepository;
import com.talkkia.api.view.UserView;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class UserInviteService {

    private static final String USER_CONTEXT = "user";
    private static final String APPLIED_STATUS = "applied";
    private static final String ALREADY_APPLIED_STATUS = "already_applied";
    private static final Pattern VERSIONED_USER_INVITE_PATTERN = Pattern.compile("^v1\\.u\\.([1-9][0-9]*)$");

    private final UserRepository userRepository;
    private final InviteAttributionRepository inviteAttributionRepository;

    public UserInviteService(UserRepository userRepository,
                             InviteAttributionRepository inviteAttributionRepository) {
        this.userRepository = userRepository;
        this.inviteAttributionRepository = inviteAttributionRepository;
    }

    public Map<String, Object> processInvite(User currentUser,
                                             boolean isNewUser,
                                             SocialLoginRequest request) {
        String inviteCode = normalize(request.getInviteCode());
        if (inviteCode == null) {
            return buildInviteResponse("no_invite", null, null);
        }

        Long inviterId = parseInviterId(inviteCode);
        if (inviterId == null) {
            return buildInviteResponse("invalid_invite", null, null);
        }

        UserView inviter = userRepository.findOneById(inviterId).orElse(null);
        if (inviter == null) {
            return buildInviteResponse("invalid_invite", USER_CONTEXT, null);
        }

        if (currentUser.getId().equals(inviterId)) {
            return buildInviteResponse("self_invite", USER_CONTEXT, null);
        }

        String attributionId = buildAttributionId(currentUser.getId(), USER_CONTEXT);
        InviteAttribution existingAttribution = inviteAttributionRepository.findById(attributionId).orElse(null);
        if (existingAttribution != null) {
            if (String.valueOf(inviterId).equals(existingAttribution.getInviterUserId())) {
                userRepository.mergeInviteContact(currentUser.getId(), inviterId);
                return buildInviteResponse(ALREADY_APPLIED_STATUS, USER_CONTEXT, inviter);
            }
            return buildInviteResponse("existing_user", USER_CONTEXT, null);
        }

        if (!isNewUser) {
            return buildInviteResponse("existing_user", USER_CONTEXT, null);
        }

        InviteAttribution attribution = inviteAttributionRepository.mergeApplied(
                attributionId,
                String.valueOf(currentUser.getId()),
                String.valueOf(inviterId),
                USER_CONTEXT,
                null,
                APPLIED_STATUS,
                new DateTimeConverter().formatDateTime());

        if (!String.valueOf(inviterId).equals(attribution.getInviterUserId())) {
            return buildInviteResponse("existing_user", USER_CONTEXT, null);
        }

        userRepository.mergeInviteContact(currentUser.getId(), inviterId);

        return buildInviteResponse(APPLIED_STATUS, USER_CONTEXT, inviter);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty() || "null".equalsIgnoreCase(normalized)) {
            return null;
        }
        return normalized;
    }

    private Long parseInviterId(String inviteCode) {
        Matcher matcher = VERSIONED_USER_INVITE_PATTERN.matcher(inviteCode);
        if (matcher.matches()) {
            return parseUserId(matcher.group(1));
        }
        return parseUserId(inviteCode);
    }

    private Long parseUserId(String value) {
        if (!value.matches("[1-9][0-9]*")) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String buildAttributionId(Long inviteeUserId, String context) {
        return inviteeUserId + ":" + context;
    }

    private Map<String, Object> buildInviteResponse(String status, String context, UserView inviter) {
        Map<String, Object> invite = new HashMap<>();
        invite.put("status", status);
        invite.put("ctx", context);
        invite.put("inviter", toContactResponse(inviter));
        return invite;
    }

    private Map<String, Object> toContactResponse(UserView user) {
        if (user == null) {
            return null;
        }
        Map<String, Object> response = new HashMap<>();
        response.put("id", String.valueOf(user.getId()));
        response.put("name", user.getName());
        response.put("mobile", user.getMobile());
        response.put("gender", user.getGender());
        response.put("region", user.getRegion());
        response.put("picture", user.getPicture());
        return response;
    }
}
