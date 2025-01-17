package com.automwrite.assessment.controller;

import com.automwrite.assessment.service.LlmService;
import com.automwrite.assessment.service.ToneChangeService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class Controller {

    private final ToneChangeService toneChangeService;

    /**
     * You should extract the tone from the `toneFile` and update the `contentFile`
     * to convey the same content
     * but using the extracted tone.
     * 
     * @param toneFile    File to extract the tone from
     * @param contentFile File to apply the tone to
     * @return A response indicating that the processing has completed
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @PostMapping("/test")
    public ResponseEntity<String> test(@RequestParam MultipartFile toneFile, @RequestParam MultipartFile contentFile)
            throws IOException, InterruptedException, ExecutionException {
        // Load the documents
        XWPFDocument toneDocument = new XWPFDocument(toneFile.getInputStream());
        XWPFDocument contentDocument = new XWPFDocument(contentFile.getInputStream());
        toneChangeService.changeTone(toneDocument, contentDocument).get();

        // Simple response to indicate that everything completed
        return ResponseEntity.ok("File successfully uploaded, processing completed");
    }
}
