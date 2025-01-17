package com.automwrite.assessment.service;

import java.util.concurrent.CompletableFuture;
import com.automwrite.assessment.dto.ToneChangeRequest;
import com.automwrite.assessment.dto.ToolResponse;

public interface LlmService {

    String generateText(String prompt);

    CompletableFuture<String> generateTextAsync(String prompt);

    CompletableFuture<String> extractTone(String text);

    CompletableFuture<ToolResponse> changeTone(ToneChangeRequest request);
}
