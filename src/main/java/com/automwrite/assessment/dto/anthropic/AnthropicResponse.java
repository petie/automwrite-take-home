package com.automwrite.assessment.dto.anthropic;

import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;

public record AnthropicResponse(List<AnthropicContent> content) {

  public boolean hasToolsCall() {
    return content.stream().anyMatch(content -> content.type().equals("tool_use"));
  }

  public JsonNode toolCallInput() {
    return content.stream().filter(content -> content.type().equals("tool_use")).findFirst().get().input();
  }

  public boolean hasText() {
    return content.stream().anyMatch(content -> content.type().equals("text"));
  }

  public String text() {
    return content.stream().filter(content -> content.type().equals("text")).findFirst().get().text();
  }
}

