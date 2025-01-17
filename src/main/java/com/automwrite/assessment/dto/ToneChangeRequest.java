package com.automwrite.assessment.dto;

import java.util.List;

public record ToneChangeRequest(String name, List<Paragraph> paragraphs, String tone) {
}
