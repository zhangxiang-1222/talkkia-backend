package com.talkkia.api.model.contact;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContactHashRecord {
    private String phoneHmac;
    private byte[] phoneCiphertext;
    private byte[] encryptionNonce;
    private UUID syncToken;
}
