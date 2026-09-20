package com.talkkia.api.services;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeywordModerationServiceTest {

    @Test
    void matchesEnglishKeywordIgnoringCaseAndPunctuation() {
        KeywordModerationService service = createService(
                "# comment\nkill yourself\nchild porn\n"
        );

        assertTrue(service.containsBlockedKeyword("He said: KILL-YOURSELF!"));
        assertTrue(service.containsBlockedKeyword("This contains child   porn."));
    }

    @Test
    void usesWordBoundariesForEnglishKeyword() {
        KeywordModerationService service = createService("ass\n");

        assertTrue(service.containsBlockedKeyword("You are an ass."));
        assertFalse(service.containsBlockedKeyword("The class starts now."));
    }

    @Test
    void matchesCjkKeywordAcrossWhitespaceAndPunctuation() {
        KeywordModerationService service = createService("制作炸弹\n");

        assertTrue(service.containsBlockedKeyword("这是制作、炸 弹的方法"));
    }

    @Test
    void normalizesFullWidthCharacters() {
        KeywordModerationService service = createService("phishing link\n");

        assertTrue(service.containsBlockedKeyword("ＰＨＩＳＨＩＮＧ　ＬＩＮＫ"));
    }

    @Test
    void allowsTextWithoutBlockedKeywords() {
        KeywordModerationService service = createService("child porn\n制作炸弹\n");

        assertFalse(service.containsBlockedKeyword("A normal conversation about the weather."));
        assertFalse(service.containsBlockedKeyword(null));
    }

    @Test
    void rejectsEmptyKeywordFile() {
        assertThrows(
                IllegalStateException.class,
                () -> createService("# comments only\n\n")
        );
    }

    @Test
    void loadsConfiguredKeywordFileAndMatchesRepresentativeCategories() {
        KeywordModerationService service = new KeywordModerationService(
                new ClassPathResource("moderation/blocked-keywords.txt")
        );

        assertTrue(service.containsBlockedKeyword("They shared a phishing site."));
        assertTrue(service.containsBlockedKeyword("这是一段制作冰毒的内容"));
        assertTrue(service.containsBlockedKeyword("有人试图泄露身份证号"));
        assertTrue(service.containsBlockedKeyword("点击这个在线赌场链接"));
        assertFalse(service.containsBlockedKeyword("Let's discuss tomorrow's weather."));
    }

    private KeywordModerationService createService(String keywords) {
        return new KeywordModerationService(new ByteArrayResource(
                keywords.getBytes(StandardCharsets.UTF_8)
        ));
    }
}
