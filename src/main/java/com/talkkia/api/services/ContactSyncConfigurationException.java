package com.talkkia.api.services;

public class ContactSyncConfigurationException extends IllegalStateException {
    public ContactSyncConfigurationException(String message) {
        super(message);
    }

    public ContactSyncConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
