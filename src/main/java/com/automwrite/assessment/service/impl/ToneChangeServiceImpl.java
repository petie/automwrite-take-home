package com.automwrite.assessment.service.impl;

import com.automwrite.assessment.service.ToneChangeService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import com.automwrite.assessment.dto.Paragraph;
import com.automwrite.assessment.dto.ToneChangeRequest;
import com.automwrite.assessment.dto.ToolResponse;
import com.automwrite.assessment.service.LlmService;

@AllArgsConstructor
@Slf4j
@Service
public class ToneChangeServiceImpl implements ToneChangeService {
  private final LlmService llmService;

  @Override
  public CompletableFuture<Void> changeTone(XWPFDocument toneDocument, XWPFDocument target) {
    try (XWPFWordExtractor ex = new XWPFWordExtractor(toneDocument)) {
      String tone = llmService.extractTone(ex.getText()).get();
      ToneChangeRequest request = createToneChangeRequest(target, tone);
      ToolResponse response = llmService.changeTone(request).get();
      applyToneChange(response, target);
      return CompletableFuture.completedFuture(null);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return CompletableFuture.failedFuture(e);
    } catch (ExecutionException | IOException e) {
      return CompletableFuture.failedFuture(e.getCause());
    }
  }

  private void applyToneChange(ToolResponse response, XWPFDocument target) {
    for (int i = 0; i < target.getParagraphs().size(); i++) {
      final int paragraphIndex = i; // Create a final variable for use in lambda
      Paragraph matchingParagraph = response.paragraphs().stream()
          .filter(p -> p.id() == paragraphIndex)
          .findFirst()
          .orElse(null);
      if (matchingParagraph != null) {
        replaceParagraphText(target.getParagraphs().get(i), matchingParagraph.text());
      }
    }

    try {
      File outputDir = new File("different tones");
      File outputFile = new File(outputDir, "processed.docx");
      try (FileOutputStream out = new FileOutputStream(outputFile)) {
        target.write(out);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to save document", e);
    }
  }

  private ToneChangeRequest createToneChangeRequest(XWPFDocument target, String tone) {
    List<XWPFParagraph> paragraphs = target.getParagraphs();

    List<Paragraph> result = new ArrayList<Paragraph>();
    boolean foundAddress = false;
    boolean isInAddress = false;
    String addressee = "";
    for (int i = 0; i < paragraphs.size(); i++) {
      XWPFParagraph paragraph = paragraphs.get(i);
      String text = paragraph.getText().trim();
      if (isSkippableContent(text)) {
      } else if (isStartOfAddress(text) && !foundAddress) {
        isInAddress = true;
        foundAddress = true;
        addressee = text;
      } else if (isUkPostcode(text) && isInAddress) {
        isInAddress = false;
      } else if (isInAddress) {
      } else {
        result.add(new Paragraph(i, text));
      }
    }
    return new ToneChangeRequest(addressee, result, tone);
  }

  private boolean isSkippableContent(String text) {
    return text.isEmpty() ||
        text.matches("^(\\d{1,2}/\\d{1,2}/\\d{2,4}.*)|Date");
  }

  private void replaceParagraphText(XWPFParagraph paragraph, String newText) {
    List<XWPFRun> runs = paragraph.getRuns();
    if (runs.isEmpty()) {
      XWPFRun run = paragraph.createRun();
      run.setText(newText);
      return;
    }
    for (XWPFRun run : runs) {
      run.setText("", 0);
    }
    runs.get(0).setText(newText, 0);
  }

  private boolean isStartOfAddress(String text) {
    return text.matches("^(?:Mr\\.|Mrs\\.|Ms\\.).*");
  }

  private boolean isUkPostcode(String text) {
    return text.matches(
        "(GIR 0AA)|((([A-Z-[QVX]][0-9][0-9]?)|(([A-Z-[QVX]][A-Z-[IJZ]][0-9][0-9]?)|(([A-Z-[QVX]][0-9][A-HJKSTUW])|([A-Z-[QVX]][A-Z-[IJZ]][0-9][ABEHMNPRVWXY])))) [0-9][A-Z-[CIKMOV]]{2})");
  }
}
