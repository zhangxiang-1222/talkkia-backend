package com.talkkia.api.enums;

import java.util.Locale;

public enum DropReplySort {
    RECOMMENDED("recommended"),
    LATEST("latest"),
    MOST_LIKED("most_liked"),
    MOST_DISCUSSED("most_discussed");

    private final String value;

    DropReplySort(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DropReplySort fromValue(String value) {
        if (value == null || value.isBlank()) {
            return RECOMMENDED;
        }

        String normalizedValue = value.trim().toLowerCase(Locale.ROOT);
        for (DropReplySort sort : values()) {
            if (sort.value.equals(normalizedValue)) {
                return sort;
            }
        }
        throw new IllegalArgumentException("Unsupported reply sort: " + value);
    }
}
