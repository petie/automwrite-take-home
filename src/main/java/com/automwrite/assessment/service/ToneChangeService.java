package com.automwrite.assessment.service;

import java.util.concurrent.CompletableFuture;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

public interface ToneChangeService {
  CompletableFuture<Void> changeTone(XWPFDocument toneDocument, XWPFDocument target);
}
