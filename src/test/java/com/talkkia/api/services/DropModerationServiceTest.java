package com.talkkia.api.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DropModerationServiceTest {

    @Mock
    private WhisperService whisperService;

    @Mock
    private R2UploadService r2UploadService;

    @Mock
    private KeywordModerationService keywordModerationService;

    @InjectMocks
    private DropModerationService dropModerationService;

    @Test
    void rejectsAudioWhenTranscriptContainsBlockedKeyword() {
        when(r2UploadService.createDownloadUrl("drop/audio.m4a"))
                .thenReturn("https://example.com/audio.m4a");
        when(whisperService.transcribeFromUrl("https://example.com/audio.m4a"))
                .thenReturn("blocked transcript");
        when(keywordModerationService.containsBlockedKeyword("blocked transcript"))
                .thenReturn(true);

        assertFalse(dropModerationService.moderateAudioContent("drop/audio.m4a"));
        verify(keywordModerationService).containsBlockedKeyword("blocked transcript");
    }

    @Test
    void approvesAudioWhenTranscriptContainsNoBlockedKeyword() {
        when(r2UploadService.createDownloadUrl("drop/audio.m4a"))
                .thenReturn("https://example.com/audio.m4a");
        when(whisperService.transcribeFromUrl("https://example.com/audio.m4a"))
                .thenReturn("safe transcript");

        assertTrue(dropModerationService.moderateAudioContent("drop/audio.m4a"));
        verify(keywordModerationService).containsBlockedKeyword("safe transcript");
    }

    @Test
    void rejectsEmptyTranscriptWithoutRunningKeywordMatcher() {
        when(r2UploadService.createDownloadUrl("drop/audio.m4a"))
                .thenReturn("https://example.com/audio.m4a");
        when(whisperService.transcribeFromUrl("https://example.com/audio.m4a"))
                .thenReturn(" ");

        assertFalse(dropModerationService.moderateAudioContent("drop/audio.m4a"));
        verify(keywordModerationService, never()).containsBlockedKeyword(" ");
    }
}
