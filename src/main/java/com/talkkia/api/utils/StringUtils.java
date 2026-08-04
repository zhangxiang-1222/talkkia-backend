package com.talkkia.api.utils;

/**
 * @Author: Ethan
 * @Date: 2024/9/17
 * @Description: String type operation
 */
public class StringUtils {

    public static boolean isBlank(final CharSequence cs) {
        final int strLen = length(cs);
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }
}
