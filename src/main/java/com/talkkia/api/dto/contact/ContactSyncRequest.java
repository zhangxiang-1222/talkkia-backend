package com.talkkia.api.dto.contact;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactSyncRequest {
    private String mode;
    private List<ContactSyncContactRequest> contacts;
    private List<String> removedSyncTokens;
}
