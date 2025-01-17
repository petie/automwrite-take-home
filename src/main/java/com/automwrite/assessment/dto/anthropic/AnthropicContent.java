package com.automwrite.assessment.dto.anthropic;

import com.fasterxml.jackson.databind.JsonNode;

public record AnthropicContent(String type, JsonNode input, String text) {
}
