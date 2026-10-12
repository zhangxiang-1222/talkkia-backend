package com.talkkia.api.services;

import com.talkkia.api.entity.InviteAttribution;
import com.talkkia.api.entity.User;
import com.talkkia.api.model.SocialLoginRequest;
import com.talkkia.api.repositories.InviteAttributionRepository;
import com.talkkia.api.repositories.UserRepository;
import com.talkkia.api.view.UserView;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserInviteServiceTest {

    private static final Long INVITEE_ID = 654321L;
    private static final Long INVITER_ID = 123456L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InviteAttributionRepository inviteAttributionRepository;

    @Mock
    private UserView inviter;

    private UserInviteService userInviteService;
    private User currentUser;

    @BeforeEach
    void setUp() {
        userInviteService = new UserInviteService(userRepository, inviteAttributionRepository);
        currentUser = new User();
        currentUser.setId(INVITEE_ID);
    }

    @Test
    void shouldReturnNoInviteWhenInviteCodeIsMissing() {
        Map<String, Object> result = userInviteService.processInvite(
                currentUser,
                true,
                new SocialLoginRequest());

        assertEquals("no_invite", result.get("status"));
        assertNull(result.get("ctx"));
        assertNull(result.get("inviter"));
        verifyNoInteractions(userRepository, inviteAttributionRepository);
    }

    @Test
    void shouldRejectUnsupportedGroupInviteCode() {
        SocialLoginRequest request = createRequest("v1.g.123456.78910");

        Map<String, Object> result = userInviteService.processInvite(currentUser, true, request);

        assertEquals("invalid_invite", result.get("status"));
        assertNull(result.get("ctx"));
        verifyNoInteractions(userRepository, inviteAttributionRepository);
    }

    @Test
    void shouldRejectInvalidInviteCodeWithoutBlockingLogin() {
        SocialLoginRequest request = createRequest("not-a-user-id");

        Map<String, Object> result = userInviteService.processInvite(currentUser, true, request);

        assertEquals("invalid_invite", result.get("status"));
        assertNull(result.get("ctx"));
        assertNull(result.get("inviter"));
        verifyNoInteractions(userRepository, inviteAttributionRepository);
    }

    @Test
    void shouldRejectSelfInvite() {
        SocialLoginRequest request = createRequest("v1.u." + INVITEE_ID);
        when(userRepository.findOneById(INVITEE_ID)).thenReturn(Optional.of(inviter));

        Map<String, Object> result = userInviteService.processInvite(currentUser, true, request);

        assertEquals("self_invite", result.get("status"));
        verify(userRepository, never()).mergeInviteContact(INVITEE_ID, INVITEE_ID);
        verifyNoInteractions(inviteAttributionRepository);
    }

    @Test
    void shouldApplyValidInviteForNewUser() {
        SocialLoginRequest request = createRequest("v1.u." + INVITER_ID);
        when(userRepository.findOneById(INVITER_ID)).thenReturn(Optional.of(inviter));
        when(inviter.getId()).thenReturn(INVITER_ID);
        when(inviter.getName()).thenReturn("Inviter");
        when(inviteAttributionRepository.findById(INVITEE_ID + ":user"))
                .thenReturn(Optional.empty());
        when(inviteAttributionRepository.mergeApplied(
                org.mockito.ArgumentMatchers.eq(INVITEE_ID + ":user"),
                org.mockito.ArgumentMatchers.eq(String.valueOf(INVITEE_ID)),
                org.mockito.ArgumentMatchers.eq(String.valueOf(INVITER_ID)),
                org.mockito.ArgumentMatchers.eq("user"),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq("applied"),
                org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(new InviteAttribution(
                        INVITEE_ID + ":user",
                        String.valueOf(INVITEE_ID),
                        String.valueOf(INVITER_ID),
                        "user",
                        null,
                        "applied",
                        "09/10/2026 10:00"));

        Map<String, Object> result = userInviteService.processInvite(currentUser, true, request);

        assertEquals("applied", result.get("status"));
        assertEquals("user", result.get("ctx"));
        Map<?, ?> inviterResponse = (Map<?, ?>) result.get("inviter");
        assertEquals(String.valueOf(INVITER_ID), inviterResponse.get("id"));
        verify(userRepository).mergeInviteContact(INVITEE_ID, INVITER_ID);
        verify(inviteAttributionRepository).mergeApplied(
                org.mockito.ArgumentMatchers.eq(INVITEE_ID + ":user"),
                org.mockito.ArgumentMatchers.eq(String.valueOf(INVITEE_ID)),
                org.mockito.ArgumentMatchers.eq(String.valueOf(INVITER_ID)),
                org.mockito.ArgumentMatchers.eq("user"),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq("applied"),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldReturnAlreadyAppliedForExistingInviteAttribution() {
        SocialLoginRequest request = createRequest(String.valueOf(INVITER_ID));
        InviteAttribution attribution = new InviteAttribution(
                INVITEE_ID + ":user",
                String.valueOf(INVITEE_ID),
                String.valueOf(INVITER_ID),
                "user",
                null,
                "applied",
                "09/10/2026 10:00");
        when(userRepository.findOneById(INVITER_ID)).thenReturn(Optional.of(inviter));
        when(inviter.getId()).thenReturn(INVITER_ID);
        when(inviteAttributionRepository.findById(INVITEE_ID + ":user"))
                .thenReturn(Optional.of(attribution));

        Map<String, Object> result = userInviteService.processInvite(currentUser, false, request);

        assertEquals("already_applied", result.get("status"));
        verify(userRepository).mergeInviteContact(INVITEE_ID, INVITER_ID);
        verify(inviteAttributionRepository, never()).mergeApplied(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.nullable(String.class),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldNotApplyInviteForExistingUserWithoutAttribution() {
        SocialLoginRequest request = createRequest("v1.u." + INVITER_ID);
        when(userRepository.findOneById(INVITER_ID)).thenReturn(Optional.of(inviter));
        when(inviteAttributionRepository.findById(INVITEE_ID + ":user"))
                .thenReturn(Optional.empty());

        Map<String, Object> result = userInviteService.processInvite(currentUser, false, request);

        assertEquals("existing_user", result.get("status"));
        verify(userRepository, never()).mergeInviteContact(INVITEE_ID, INVITER_ID);
        verify(inviteAttributionRepository, never()).mergeApplied(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.nullable(String.class),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    private SocialLoginRequest createRequest(String inviteCode) {
        SocialLoginRequest request = new SocialLoginRequest();
        request.setInviteCode(inviteCode);
        return request;
    }
}
