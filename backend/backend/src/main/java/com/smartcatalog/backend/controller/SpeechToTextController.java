package com.smartcatalog.backend.controller;

import com.smartcatalog.backend.ai.SpeechToTextService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class SpeechToTextController {

    private final SpeechToTextService speechToTextService;

    public SpeechToTextController(
            SpeechToTextService speechToTextService) {

        this.speechToTextService = speechToTextService;
    }

    @PostMapping("/speech-to-text")
    public ResponseEntity<Map<String, Object>> speechToText(
            @RequestParam("audio") MultipartFile audio) {

        return ResponseEntity.ok(
                speechToTextService.convertSpeech(audio)
        );
    }
}
