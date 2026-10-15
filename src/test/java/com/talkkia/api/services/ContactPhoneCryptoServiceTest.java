package com.talkkia.api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.talkkia.api.model.contact.ContactHashRecord;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class ContactPhoneCryptoServiceTest {

    @Test
    void shouldUseStableHmacAndRandomNonceForTheSamePhone() {
        String encryptionKey = Base64.getEncoder().encodeToString(new byte[32]);
        String hmacSecret = Base64.getEncoder().encodeToString(new byte[32]);
        ContactPhoneCryptoService cryptoService = new ContactPhoneCryptoService(encryptionKey, hmacSecret);

        ContactHashRecord first = cryptoService.protect("+8613812345678");
        ContactHashRecord second = cryptoService.protect("+8613812345678");

        assertEquals(first.getPhoneHmac(), second.getPhoneHmac());
        assertNotEquals(first.getSyncToken(), second.getSyncToken());
        assertNotEquals(Base64.getEncoder().encodeToString(first.getEncryptionNonce()),
                Base64.getEncoder().encodeToString(second.getEncryptionNonce()));
        assertNotEquals(Base64.getEncoder().encodeToString(first.getPhoneCiphertext()),
                Base64.getEncoder().encodeToString(second.getPhoneCiphertext()));
        assertEquals("+8613812345678",
                cryptoService.decrypt(first.getPhoneCiphertext(), first.getEncryptionNonce()));
    }

    @Test
    void shouldRejectMissingEncryptionConfiguration() {
        ContactPhoneCryptoService cryptoService = new ContactPhoneCryptoService("", "");

        assertThrows(ContactSyncConfigurationException.class,
                () -> cryptoService.protect("+8613812345678"));
    }
}
