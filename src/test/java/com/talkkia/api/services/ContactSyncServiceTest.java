package com.talkkia.api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.talkkia.api.dto.contact.ContactSyncContactRequest;
import com.talkkia.api.dto.contact.ContactSyncRequest;
import com.talkkia.api.dto.contact.ContactSyncResponse;
import com.talkkia.api.model.contact.ContactHashRecord;
import com.talkkia.api.repositories.ContactHashRepository;
import com.talkkia.api.repositories.UserRepository;
import com.talkkia.api.view.ContactDiscoveryMatchView;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContactSyncServiceTest {

    private static final Long OWNER_ID = 123L;
    private static final String PHONE = "+8613812345678";
    private static final String PHONE_HMAC = "e0f39e5b6c374ae2b2b5a021404ee6209d7522702d49b3509cb259b18c1a55e1";
    private static final UUID SYNC_TOKEN = UUID.fromString("5767c232-901f-4ac3-8eaa-d8498c8a1bfe");

    @Mock
    private ContactHashRepository contactHashRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContactPhoneNormalizer contactPhoneNormalizer;

    @Mock
    private ContactPhoneCryptoService contactPhoneCryptoService;

    private ContactSyncService contactSyncService;

    @BeforeEach
    void setUp() {
        contactSyncService = new ContactSyncService(
                contactHashRepository,
                userRepository,
                contactPhoneNormalizer,
                contactPhoneCryptoService
        );
    }

    @Test
    void shouldReplaceContactsForFullSyncAndReturnOpaqueTokens() {
        ContactHashRecord record = new ContactHashRecord(PHONE_HMAC, new byte[]{1}, new byte[12], SYNC_TOKEN);
        when(contactPhoneNormalizer.normalize("+86 138 1234 5678")).thenReturn(PHONE);
        when(contactPhoneCryptoService.protect(PHONE)).thenReturn(record);
        when(contactHashRepository.findSyncTokens(eq(OWNER_ID), any()))
                .thenReturn(Collections.singletonMap(PHONE_HMAC, SYNC_TOKEN));
        when(contactHashRepository.removeContactsNotIn(eq(OWNER_ID), any()))
                .thenReturn(Collections.emptyList());

        ContactSyncResponse result = contactSyncService.sync(
                OWNER_ID,
                new ContactSyncRequest(
                        "full",
                        Collections.singletonList(new ContactSyncContactRequest("device-contact-1", "+86 138 1234 5678")),
                        Collections.emptyList()
                )
        );

        assertEquals("device-contact-1", result.getSyncedContacts().get(0).getClientContactId());
        assertEquals(SYNC_TOKEN.toString(), result.getSyncedContacts().get(0).getSyncToken());
        assertEquals(Collections.emptyList(), result.getMatchedUsers());
        assertEquals(Collections.emptyList(), result.getRemovedSyncTokens());
        verify(contactHashRepository).upsertContacts(eq(OWNER_ID), any());
        verify(contactHashRepository).removeContactsNotIn(eq(OWNER_ID), any());
    }

    @Test
    void shouldDeleteOnlyOwnerContactsForDeltaSync() {
        ContactSyncRequest request = new ContactSyncRequest(
                "delta",
                Collections.emptyList(),
                Collections.singletonList(SYNC_TOKEN.toString())
        );
        when(contactHashRepository.removeContactsBySyncTokens(eq(OWNER_ID), any()))
                .thenReturn(Collections.singletonList(SYNC_TOKEN));

        ContactSyncResponse result = contactSyncService.sync(OWNER_ID, request);

        assertEquals(Collections.singletonList(SYNC_TOKEN.toString()), result.getRemovedSyncTokens());
        verify(contactHashRepository).removeContactsBySyncTokens(
                OWNER_ID,
                Collections.singleton(SYNC_TOKEN)
        );
    }

    @Test
    void shouldRejectInvalidPhoneBeforeWritingContacts() {
        when(contactPhoneNormalizer.normalize("not-a-phone"))
                .thenThrow(new IllegalArgumentException("Each phone number must use E.164 format."));

        ContactSyncRequest request = new ContactSyncRequest(
                "full",
                Collections.singletonList(new ContactSyncContactRequest("device-contact-1", "not-a-phone")),
                Collections.emptyList()
        );

        assertThrows(IllegalArgumentException.class, () -> contactSyncService.sync(OWNER_ID, request));
    }

    @Test
    void shouldReturnMatchedUserWithoutReturningPhoneNumber() {
        ContactHashRecord record = new ContactHashRecord(PHONE_HMAC, new byte[]{1}, new byte[12], SYNC_TOKEN);
        ContactDiscoveryMatchView match = org.mockito.Mockito.mock(ContactDiscoveryMatchView.class);
        when(contactPhoneNormalizer.normalize(PHONE)).thenReturn(PHONE);
        when(contactPhoneCryptoService.protect(PHONE)).thenReturn(record);
        when(contactHashRepository.findSyncTokens(eq(OWNER_ID), any()))
                .thenReturn(Collections.singletonMap(PHONE_HMAC, SYNC_TOKEN));
        when(contactHashRepository.removeContactsBySyncTokens(eq(OWNER_ID), any()))
                .thenReturn(Collections.emptyList());
        when(match.getId()).thenReturn(456L);
        when(match.getMobile()).thenReturn(PHONE);
        when(match.getName()).thenReturn("Alice");
        when(match.getPicture()).thenReturn("456/profile.png");
        when(match.getContact()).thenReturn(false);
        when(userRepository.findContactDiscoveryMatches(OWNER_ID, Collections.singletonList(PHONE)))
                .thenReturn(Collections.singletonList(match));

        ContactSyncResponse result = contactSyncService.sync(
                OWNER_ID,
                new ContactSyncRequest(
                        "delta",
                        Collections.singletonList(new ContactSyncContactRequest("device-contact-1", PHONE)),
                        Collections.emptyList()
                )
        );

        assertEquals(1, result.getMatchedUsers().size());
        assertEquals(456L, result.getMatchedUsers().get(0).getId());
        assertEquals("Alice", result.getMatchedUsers().get(0).getName());
        assertEquals(SYNC_TOKEN.toString(), result.getMatchedUsers().get(0).getSyncToken());
    }
}
