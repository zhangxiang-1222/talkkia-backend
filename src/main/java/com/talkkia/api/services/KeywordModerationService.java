package com.talkkia.api.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class KeywordModerationService {

    private final List<BlockedKeyword> blockedKeywords;

    public KeywordModerationService(
            @Value("${moderation.keywords-resource:classpath:moderation/blocked-keywords.txt}")
            Resource keywordsResource
    ) {
        blockedKeywords = loadKeywords(keywordsResource);
    }

    public boolean containsBlockedKeyword(String transcript) {
        if (!StringUtils.hasText(transcript)) {
            return false;
        }

        String words = normalizeWords(transcript);
        String compact = normalizeCompact(transcript);
        return blockedKeywords.stream().anyMatch(keyword -> keyword.matches(words, compact));
    }

    private List<BlockedKeyword> loadKeywords(Resource resource) {
        List<BlockedKeyword> keywords = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                resource.getInputStream(),
                StandardCharsets.UTF_8
        ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String value = line.trim();
                if (!value.isEmpty() && !value.startsWith("#")) {
                    keywords.add(BlockedKeyword.from(value));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load moderation keywords from " + resource.getDescription(),
                    e
            );
        }

        if (keywords.isEmpty()) {
            throw new IllegalStateException("Moderation keyword file must not be empty");
        }
        return List.copyOf(keywords);
    }

    private static String normalizeWords(String value) {
        String normalized = normalizeUnicode(value);
        StringBuilder result = new StringBuilder(normalized.length());
        boolean previousSeparator = true;
        for (int offset = 0; offset < normalized.length();) {
            int codePoint = normalized.codePointAt(offset);
            offset += Character.charCount(codePoint);
            if (Character.isLetterOrDigit(codePoint)) {
                result.appendCodePoint(codePoint);
                previousSeparator = false;
            } else if (!previousSeparator) {
                result.append(' ');
                previousSeparator = true;
            }
        }
        return result.toString().trim();
    }

    private static String normalizeCompact(String value) {
        String normalized = normalizeUnicode(value);
        StringBuilder result = new StringBuilder(normalized.length());
        normalized.codePoints()
                .filter(Character::isLetterOrDigit)
                .forEach(result::appendCodePoint);
        return result.toString();
    }

    private static String normalizeUnicode(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);
    }

    private static boolean containsCjk(String value) {
        return value.codePoints().anyMatch(codePoint -> {
            Character.UnicodeScript script = Character.UnicodeScript.of(codePoint);
            return script == Character.UnicodeScript.HAN
                    || script == Character.UnicodeScript.HIRAGANA
                    || script == Character.UnicodeScript.KATAKANA
                    || script == Character.UnicodeScript.HANGUL;
        });
    }

    private static class BlockedKeyword {

        private final String compactValue;
        private final Pattern wordPattern;

        private BlockedKeyword(String compactValue, Pattern wordPattern) {
            this.compactValue = compactValue;
            this.wordPattern = wordPattern;
        }

        private static BlockedKeyword from(String value) {
            String words = normalizeWords(value);
            String compact = normalizeCompact(value);
            if (compact.isEmpty()) {
                throw new IllegalStateException(
                        "Moderation keyword must contain at least one letter or digit"
                );
            }

            if (containsCjk(value)) {
                return new BlockedKeyword(compact, null);
            }

            Pattern pattern = Pattern.compile(
                    "(?<![\\p{L}\\p{N}])" + Pattern.quote(words)
                            + "(?![\\p{L}\\p{N}])"
            );
            return new BlockedKeyword(null, pattern);
        }

        private boolean matches(String words, String compact) {
            if (wordPattern != null) {
                return wordPattern.matcher(words).find();
            }
            return compact.contains(compactValue);
        }
    }
}
