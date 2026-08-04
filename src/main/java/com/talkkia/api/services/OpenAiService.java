package com.talkkia.api.services;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talkkia.api.dto.openai.OpenAiResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

@Service
public class OpenAiService {

    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";
    private static final int MAX_ERROR_BODY_PREVIEW_LENGTH = 1500;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${openai.base-url}")
    private String baseUrl;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.timeout-ms}")
    private Long timeoutMs;

    @Value("${openai.max-output-tokens}")
    private Integer maxOutputTokens;

    public OpenAiResponseResult generateResponse(String message, String instructions) {
        validateConfiguration(message, instructions);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model.trim());
        requestBody.put("messages", List.of(
                createMessage("system", instructions),
                createMessage("user", message)
        ));
        requestBody.put("response_format", Map.of("type", "json_object"));
        requestBody.put("max_tokens", maxOutputTokens);
        requestBody.put("temperature", 0);
        requestBody.put("stream", false);

        WebClient webClient = webClientBuilder.clone().build();
        JsonNode payload = webClient.post()
                .uri(resolveChatCompletionsUrl())
                .headers(headers -> headers.setBearerAuth(apiKey.trim()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchangeToMono(response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .map(rawBody -> parseResponse(
                                response.rawStatusCode(),
                                response.headers().contentType().orElse(null),
                                rawBody
                        )))
                .timeout(Duration.ofMillis(timeoutMs))
                .onErrorMap(
                        TimeoutException.class,
                        error -> new IllegalStateException("OpenAI request timed out", error)
                )
                .onErrorMap(
                        WebClientRequestException.class,
                        error -> new IllegalStateException("Failed to reach OpenAI", error)
                )
                .block();

        if (payload == null) {
            throw new IllegalStateException("OpenAI returned an empty response");
        }

        String text = extractMessageContent(payload);
        if (!StringUtils.hasText(text)) {
            throw new IllegalStateException("OpenAI returned an empty response");
        }
        String normalizedJson = normalizeJsonContent(text);

        String responseModel = getText(payload.get("model"));
        return new OpenAiResponseResult(
                normalizedJson,
                getNullableText(payload.get("id")),
                StringUtils.hasText(responseModel) ? responseModel : model.trim()
        );
    }

    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private JsonNode parseResponse(int status, MediaType contentType, String rawBody) {
        if (contentType == null || !MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
            throw new IllegalStateException(
                    "OpenAI returned a non-JSON response. status=" + status
                            + ", content-type=" + (contentType == null ? "unknown" : contentType)
                            + ", body=" + formatErrorBodyPreview(rawBody)
            );
        }

        JsonNode payload;
        try {
            payload = objectMapper.readTree(rawBody);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Invalid JSON from OpenAI. status=" + status
                            + ", content-type=" + contentType
                            + ", body=" + formatErrorBodyPreview(rawBody),
                    e
            );
        }
        if (payload == null || payload.isNull()) {
            throw new IllegalStateException(
                    "Invalid JSON from OpenAI. status=" + status
                            + ", content-type=" + contentType
                            + ", body=" + formatErrorBodyPreview(rawBody)
            );
        }

        if (status < 200 || status >= 300) {
            throw new IllegalStateException(
                    "OpenAI request failed. status=" + status
                            + ", message=" + getOpenAiErrorMessage(payload)
            );
        }

        return payload;
    }

    private String extractMessageContent(JsonNode payload) {
        JsonNode choices = payload.get("choices");
        if (choices == null || !choices.isArray() || choices.size() == 0) {
            throw new IllegalStateException("OpenAI response does not contain choices");
        }

        JsonNode firstChoice = choices.get(0);
        String content = getText(firstChoice.path("message").get("content"));
        if (!StringUtils.hasText(content)) {
            String finishReason = getText(firstChoice.get("finish_reason"));
            throw new IllegalStateException(
                    "OpenAI response does not contain message content. finish_reason="
                            + (StringUtils.hasText(finishReason) ? finishReason : "unknown")
            );
        }
        return content;
    }

    private String normalizeJsonContent(String content) {
        String jsonText = content.trim();
        if (jsonText.startsWith("```")) {
            jsonText = jsonText.lines()
                    .filter(line -> !line.trim().startsWith("```"))
                    .collect(Collectors.joining("\n"))
                    .trim();
        }

        JsonNode jsonContent;
        try {
            jsonContent = objectMapper.readTree(jsonText);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("OpenAI message content is not valid JSON", e);
        }
        if (jsonContent == null || !jsonContent.isObject()) {
            throw new IllegalStateException("OpenAI message content must be a JSON object");
        }
        return jsonContent.toString();
    }

    private String getOpenAiErrorMessage(JsonNode payload) {
        String message = getText(payload.path("error").get("message"));
        return StringUtils.hasText(message) ? message : "OpenAI request failed";
    }

    private String formatErrorBodyPreview(String body) {
        String normalized = body == null ? "" : body.replaceAll("\\s+", " ").trim();
        if (!StringUtils.hasText(normalized)) {
            return "<empty>";
        }
        if (normalized.length() <= MAX_ERROR_BODY_PREVIEW_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, MAX_ERROR_BODY_PREVIEW_LENGTH) + "...";
    }

    private String resolveChatCompletionsUrl() {
        String trimmedBaseUrl = baseUrl.trim();
        String normalizedBaseUrl = trimmedBaseUrl.endsWith("/")
                ? trimmedBaseUrl.substring(0, trimmedBaseUrl.length() - 1)
                : trimmedBaseUrl;
        if (normalizedBaseUrl.endsWith(CHAT_COMPLETIONS_PATH)) {
            return normalizedBaseUrl;
        }
        return normalizedBaseUrl + CHAT_COMPLETIONS_PATH;
    }

    private void validateConfiguration(String message, String instructions) {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("OPENAI_API_KEY is required");
        }
        if (!StringUtils.hasText(baseUrl)) {
            throw new IllegalStateException("OPENAI_BASE_URL is required");
        }
        if (!StringUtils.hasText(model)) {
            throw new IllegalStateException("OPENAI_MODEL is required");
        }
        if (timeoutMs == null || timeoutMs <= 0) {
            throw new IllegalStateException("OPENAI_TIMEOUT_MS must be positive");
        }
        if (maxOutputTokens == null || maxOutputTokens <= 0) {
            throw new IllegalStateException("OPENAI_MAX_OUTPUT_TOKENS must be positive");
        }
        if (!StringUtils.hasText(message)) {
            throw new IllegalArgumentException("OpenAI input message is required");
        }
        if (!StringUtils.hasText(instructions)) {
            throw new IllegalArgumentException("OpenAI instructions are required");
        }
    }

    private String getText(JsonNode node) {
        return node != null && node.isTextual() ? node.asText().trim() : "";
    }

    private String getNullableText(JsonNode node) {
        String value = getText(node);
        return StringUtils.hasText(value) ? value : null;
    }
}
