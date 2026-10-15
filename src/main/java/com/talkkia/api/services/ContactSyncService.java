package com.talkkia.api.services;

import com.talkkia.api.dto.contact.ContactSyncContactRequest;
import com.talkkia.api.dto.contact.ContactSyncContactResponse;
import com.talkkia.api.dto.contact.ContactSyncMatchedUserResponse;
import com.talkkia.api.dto.contact.ContactSyncRequest;
import com.talkkia.api.dto.contact.ContactSyncResponse;
import com.talkkia.api.model.contact.ContactHashRecord;
import com.talkkia.api.repositories.ContactHashRepository;
import com.talkkia.api.repositories.UserRepository;
import com.talkkia.api.view.ContactDiscoveryMatchView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactSyncService {

    private static final int MAX_CONTACTS_PER_SYNC = 3000;
    private static final int MAX_CLIENT_CONTACT_ID_LENGTH = 256;

    private final ContactHashRepository contactHashRepository;
    private final UserRepository userRepository;
    private final ContactPhoneNormalizer contactPhoneNormalizer;
    private final ContactPhoneCryptoService contactPhoneCryptoService;

    public ContactSyncService(ContactHashRepository contactHashRepository,
                              UserRepository userRepository,
                              ContactPhoneNormalizer contactPhoneNormalizer,
                              ContactPhoneCryptoService contactPhoneCryptoService) {
        this.contactHashRepository = contactHashRepository;
        this.userRepository = userRepository;
        this.contactPhoneNormalizer = contactPhoneNormalizer;
        this.contactPhoneCryptoService = contactPhoneCryptoService;
    }

    @Transactional
    public ContactSyncResponse sync(Long ownerId, ContactSyncRequest request) {
        String mode = normalizeMode(request);
        List<ContactSyncContactRequest> contacts = requireContacts(request);
        validateContactCount(contacts.size(), "contacts");

        Map<String, String> phoneByClientContactId = normalizeContacts(contacts);
        Set<UUID> requestedRemovedSyncTokens = validateRemovalRequest(mode, request);
        Map<String, ContactHashRecord> recordByPhone = protectUniquePhones(phoneByClientContactId.values());
        List<String> uploadedPhoneHmacs = phoneHmacs(recordByPhone);

        contactHashRepository.upsertContacts(ownerId, recordByPhone.values());
        Map<String, UUID> syncTokenByPhoneHmac = uploadedPhoneHmacs.isEmpty()
                ? Collections.emptyMap()
                : contactHashRepository.findSyncTokens(ownerId, uploadedPhoneHmacs);
        if (syncTokenByPhoneHmac == null) {
            throw new IllegalStateException("Contact sync token lookup failed.");
        }

        List<UUID> removedSyncTokens;
        if ("full".equals(mode)) {
            removedSyncTokens = contactHashRepository.removeContactsNotIn(ownerId, uploadedPhoneHmacs);
        } else {
            requestedRemovedSyncTokens.removeAll(new LinkedHashSet<>(syncTokenByPhoneHmac.values()));
            removedSyncTokens = contactHashRepository.removeContactsBySyncTokens(ownerId, requestedRemovedSyncTokens);
        }

        List<ContactSyncContactResponse> responseContacts = buildResponseContacts(
                phoneByClientContactId,
                recordByPhone,
                syncTokenByPhoneHmac
        );
        List<ContactSyncMatchedUserResponse> matchedUsers = findMatchedUsers(
                ownerId,
                phoneByClientContactId,
                recordByPhone,
                syncTokenByPhoneHmac
        );

        return new ContactSyncResponse(
                responseContacts,
                matchedUsers,
                toTokenStrings(removedSyncTokens)
        );
    }

    private String normalizeMode(ContactSyncRequest request) {
        if (request == null || request.getMode() == null) {
            throw new IllegalArgumentException("Sync mode must be full or delta.");
        }
        String mode = request.getMode().trim().toLowerCase(Locale.ROOT);
        if (!"full".equals(mode) && !"delta".equals(mode)) {
            throw new IllegalArgumentException("Sync mode must be full or delta.");
        }
        return mode;
    }

    private List<ContactSyncContactRequest> requireContacts(ContactSyncRequest request) {
        if (request.getContacts() == null) {
            throw new IllegalArgumentException("Contacts must be provided.");
        }
        return request.getContacts();
    }

    private void validateContactCount(int count, String fieldName) {
        if (count > MAX_CONTACTS_PER_SYNC) {
            throw new IllegalArgumentException(fieldName + " may contain at most " + MAX_CONTACTS_PER_SYNC + " items.");
        }
    }

    private Map<String, String> normalizeContacts(List<ContactSyncContactRequest> contacts) {
        Map<String, String> phoneByClientContactId = new LinkedHashMap<>();
        for (ContactSyncContactRequest contact : contacts) {
            if (contact == null) {
                throw new IllegalArgumentException("Contacts must not contain null items.");
            }
            String clientContactId = normalizeClientContactId(contact.getClientContactId());
            if (phoneByClientContactId.containsKey(clientContactId)) {
                throw new IllegalArgumentException("clientContactId values must be unique.");
            }
            phoneByClientContactId.put(clientContactId, contactPhoneNormalizer.normalize(contact.getPhone()));
        }
        return phoneByClientContactId;
    }

    private String normalizeClientContactId(String clientContactId) {
        if (clientContactId == null || clientContactId.trim().isEmpty()) {
            throw new IllegalArgumentException("Each contact must include a clientContactId.");
        }
        String normalized = clientContactId.trim();
        if (normalized.length() > MAX_CLIENT_CONTACT_ID_LENGTH) {
            throw new IllegalArgumentException("clientContactId is too long.");
        }
        return normalized;
    }

    private Map<String, ContactHashRecord> protectUniquePhones(Iterable<String> normalizedPhones) {
        Map<String, ContactHashRecord> recordByPhone = new LinkedHashMap<>();
        for (String normalizedPhone : normalizedPhones) {
            if (!recordByPhone.containsKey(normalizedPhone)) {
                recordByPhone.put(normalizedPhone, contactPhoneCryptoService.protect(normalizedPhone));
            }
        }
        return recordByPhone;
    }

    private void validateNoRemovedTokens(ContactSyncRequest request) {
        if (request.getRemovedSyncTokens() != null && !request.getRemovedSyncTokens().isEmpty()) {
            throw new IllegalArgumentException("removedSyncTokens is only valid for delta sync.");
        }
    }

    private Set<UUID> validateRemovalRequest(String mode, ContactSyncRequest request) {
        if ("full".equals(mode)) {
            validateNoRemovedTokens(request);
            return Collections.emptySet();
        }
        return parseRemovedSyncTokens(request.getRemovedSyncTokens());
    }

    private Set<UUID> parseRemovedSyncTokens(List<String> removedSyncTokens) {
        if (removedSyncTokens == null || removedSyncTokens.isEmpty()) {
            return new LinkedHashSet<>();
        }
        validateContactCount(removedSyncTokens.size(), "removedSyncTokens");

        Set<UUID> tokens = new LinkedHashSet<>();
        for (String value : removedSyncTokens) {
            try {
                tokens.add(UUID.fromString(value));
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new IllegalArgumentException("removedSyncTokens must contain UUID values.");
            }
        }
        return tokens;
    }

    private List<String> phoneHmacs(Map<String, ContactHashRecord> recordByPhone) {
        List<String> phoneHmacs = new ArrayList<>();
        for (ContactHashRecord record : recordByPhone.values()) {
            phoneHmacs.add(record.getPhoneHmac());
        }
        return phoneHmacs;
    }

    private List<ContactSyncContactResponse> buildResponseContacts(
            Map<String, String> phoneByClientContactId,
            Map<String, ContactHashRecord> recordByPhone,
            Map<String, UUID> syncTokenByPhoneHmac
    ) {
        List<ContactSyncContactResponse> responseContacts = new ArrayList<>();
        for (Map.Entry<String, String> entry : phoneByClientContactId.entrySet()) {
            ContactHashRecord record = recordByPhone.get(entry.getValue());
            UUID syncToken = syncTokenByPhoneHmac.get(record.getPhoneHmac());
            if (syncToken == null) {
                throw new IllegalStateException("Contact sync token was not stored.");
            }
            responseContacts.add(new ContactSyncContactResponse(entry.getKey(), syncToken.toString()));
        }
        return responseContacts;
    }

    private List<ContactSyncMatchedUserResponse> findMatchedUsers(
            Long ownerId,
            Map<String, String> phoneByClientContactId,
            Map<String, ContactHashRecord> recordByPhone,
            Map<String, UUID> syncTokenByPhoneHmac
    ) {
        if (phoneByClientContactId.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> phones = new ArrayList<>(new LinkedHashSet<>(phoneByClientContactId.values()));
        Map<String, List<ContactDiscoveryMatchView>> matchesByPhone = new LinkedHashMap<>();
        List<ContactDiscoveryMatchView> discoveredUsers = userRepository.findContactDiscoveryMatches(ownerId, phones);
        if (discoveredUsers == null) {
            return Collections.emptyList();
        }
        for (ContactDiscoveryMatchView match : discoveredUsers) {
            if (match != null && match.getMobile() != null) {
                matchesByPhone.computeIfAbsent(match.getMobile(), key -> new ArrayList<>()).add(match);
            }
        }

        List<ContactSyncMatchedUserResponse> matchedUsers = new ArrayList<>();
        Set<String> returnedMatches = new LinkedHashSet<>();
        for (String phone : phones) {
            List<ContactDiscoveryMatchView> matchedUsersForPhone = matchesByPhone.get(phone);
            if (matchedUsersForPhone != null && !matchedUsersForPhone.isEmpty()) {
                ContactHashRecord record = recordByPhone.get(phone);
                UUID syncToken = syncTokenByPhoneHmac.get(record.getPhoneHmac());
                if (syncToken == null) {
                    throw new IllegalStateException("Contact sync token was not stored.");
                }
                for (ContactDiscoveryMatchView match : matchedUsersForPhone) {
                    String resultKey = syncToken + ":" + match.getId();
                    if (returnedMatches.add(resultKey)) {
                        matchedUsers.add(new ContactSyncMatchedUserResponse(
                            match.getId(),
                            match.getName(),
                            match.getPicture(),
                            syncToken.toString()
                        ));
                    }
                }
            }
        }
        return matchedUsers;
    }

    private List<String> toTokenStrings(List<UUID> tokens) {
        List<String> tokenStrings = new ArrayList<>();
        for (UUID token : tokens) {
            tokenStrings.add(token.toString());
        }
        return tokenStrings;
    }
}
