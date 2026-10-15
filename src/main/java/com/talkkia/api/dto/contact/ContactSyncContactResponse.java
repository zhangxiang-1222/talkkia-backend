package com.talkkia.api.dto.contact;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactSyncContactResponse {
    private String clientContactId;
    private String syncToken;
}
