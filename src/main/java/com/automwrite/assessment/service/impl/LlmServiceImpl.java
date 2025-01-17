package com.automwrite.assessment.service.impl;

import com.automwrite.assessment.dto.ToneChangeRequest;
import com.automwrite.assessment.dto.ToolResponse;
import com.automwrite.assessment.dto.anthropic.AnthropicResponse;
import com.automwrite.assessment.service.LlmService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class LlmServiceImpl implements LlmService {

    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";
    private static final Map<String, Object> REWRITE_TOOL = Map.of(
            "name", "rewrite_text",
            "description", "Rewrite the text in a different tone",
            "input_schema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                            "paragraphs", Map.of(
                                    "type", "array",
                                    "items", Map.of(
                                            "type", "object",
                                            "properties", Map.of(
                                                    "id", Map.of(
                                                            "type", "integer",
                                                            "description", "The ID of the paragraph to rewrite"),
                                                    "text", Map.of(
                                                            "type", "string",
                                                            "description", "The text to rewrite")),
                                            "required", List.of("id", "text")))),
                    "required", List.of("paragraphs")));

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public LlmServiceImpl(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${anthropic.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }

    @Override
    public String generateText(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");

            Map<String, Object> requestBody = Map.of(
                    "model", "claude-3-5-sonnet-20241022",
                    "max_tokens", 8192, // Claude supports up to 8192 output tokens
                    "messages", new Object[] {
                            Map.of("role", "user", "content", prompt)
                    });

            var response = restTemplate.postForObject(
                    ANTHROPIC_API_URL,
                    new HttpEntity<>(requestBody, headers),
                    AnthropicResponse.class);

            if (response != null && response.hasText()) {
                return response.text();
            }

            log.error("Unexpected response format: {}", response);
            return "";
        } catch (Exception e) {
            log.error("Error generating text", e);
            return "";
        }
    }

    private ToolResponse generateJson(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");

            Map<String, Object> requestBody = Map.of(
                    "model", "claude-3-5-sonnet-20241022",
                    "max_tokens", 8192, // Claude supports up to 8192 output tokens
                    "messages", new Object[] {
                            Map.of("role", "user", "content", prompt)
                    },
                    "tool_choice", Map.of("type", "tool", "name", "rewrite_text"),
                    "tools", List.of(REWRITE_TOOL));

            var response = restTemplate.postForObject(
                    ANTHROPIC_API_URL,
                    new HttpEntity<>(requestBody, headers),
                    AnthropicResponse.class);

            if (response != null && response.hasToolsCall()) {
                return objectMapper.readerFor(ToolResponse.class).readValue(response.toolCallInput());
            }

            log.error("Unexpected response format: {}", response);
            return null;
        } catch (Exception e) {
            log.error("Error generating text", e);
            return null;
        }
    }

    @Override
    public CompletableFuture<String> generateTextAsync(String prompt) {
        return CompletableFuture.supplyAsync(() -> generateText(prompt));
    }

    @Override
    public CompletableFuture<String> extractTone(String text) {
        return generateTextAsync(
                String.format(
                        """
                                Extract the tone from the following text.
                                This will be used to rewrite a different document to match the tone.
                                Describe the tone in a general way, not specific to the text.
                                Be concise:
                                %s
                                """,
                        text));
    }

    @Override
    public CompletableFuture<ToolResponse> changeTone(ToneChangeRequest request) {
        String jsonString;
        try {
            jsonString = objectMapper.writer().writeValueAsString(request.paragraphs());
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert textObjects to JSON", e);
        }

        String prompt = String.format(
                """
                                Below is a JSON representation of paragraphs from a docx file of a letter.
                                Rewrite the text in the following tone
                                <tone>%s</tone>
                                Preserving the same meaning:
                                <json>%s</json>
                                Use the rewrite_text tool to submit your response. If no change is needed to a paragraph, do not include it in the response.
                                Preserve any names, phone numbers, addresses, etc. Do not add ady numbering or bullet points if they didn't exist in the original paragraph.
                                The letter is addressed to %s.
                        """,
                request.tone(), jsonString, request.name());
        ToolResponse response = generateJson(prompt);
        return CompletableFuture.completedFuture(response);
    }
}