package com.talkkia.api.dto.contact;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactSyncResponse {
    private List<ContactSyncContactResponse> syncedContacts;
    private List<ContactSyncMatchedUserResponse> matchedUsers;
    private List<String> removedSyncTokens;
}
