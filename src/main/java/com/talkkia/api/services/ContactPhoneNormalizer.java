package com.talkkia.api.services;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ContactPhoneNormalizer {

    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9][0-9]{7,14}$");
    private static final Pattern FORMATTING_PATTERN = Pattern.compile("[\\s().-]");

    public String normalize(String phone) {
        if (phone == null) {
            throw new IllegalArgumentException("Each contact must include a phone number.");
        }

        String normalized = FORMATTING_PATTERN.matcher(phone.trim()).replaceAll("");
        if (!E164_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Each phone number must use E.164 format.");
        }
        return normalized;
    }
}
