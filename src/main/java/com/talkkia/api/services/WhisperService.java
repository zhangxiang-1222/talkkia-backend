package com.talkkia.api.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class WhisperService {

    private static final int MAX_AUDIO_SIZE = 10 * 1024 * 1024;
    private static final String TRANSCRIPTION_PATH = "/v1/audio/transcriptions";

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${whisper.base-url}")
    private String transcriptionBaseUrl;

    @Value("${whisper.api-key}")
    private String apiKey;

    public String transcribeFromUrl(String audioUrl) {
        WebClient webClient = webClientBuilder.clone()
                .codecs(configurer ->
                        configurer.defaultCodecs().maxInMemorySize(MAX_AUDIO_SIZE + 1024))
                .build();
        byte[] audioBytes = webClient.get()
                .uri(audioUrl)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

        if (audioBytes == null || audioBytes.length == 0) {
            throw new IllegalStateException("Audio file is empty");
        }

        if (audioBytes.length > MAX_AUDIO_SIZE) {
            throw new IllegalArgumentException("Audio file must not exceed 10 MB");
        }

        ByteArrayResource audioResource = new ByteArrayResource(audioBytes) {
            @Override
            public String getFilename() {
                return "audio.m4a";
            }
        };

        MultipartBodyBuilder multipart = new MultipartBodyBuilder();
        multipart.part("file", audioResource)
                .filename("audio.m4a")
                .contentType(MediaType.parseMediaType("audio/mp4"));
        multipart.part("model", "whisper-1");

        JsonNode response = webClient.post()
                .uri(resolveTranscriptionUrl())
                .headers(headers ->
                        headers.setBearerAuth(apiKey))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipart.build()))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null || !response.hasNonNull("text")) {
            throw new IllegalStateException("Whisper response does not contain text");
        }

        return response.get("text").asText();
    }

    private String resolveTranscriptionUrl() {
        String baseUrl = transcriptionBaseUrl.endsWith("/")
                ? transcriptionBaseUrl.substring(0, transcriptionBaseUrl.length() - 1)
                : transcriptionBaseUrl;
        return baseUrl + TRANSCRIPTION_PATH;
    }
}
