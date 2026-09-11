package com.smartcatalog.backend.controller;

import com.smartcatalog.backend.ai.ImageAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class ImageAnalysisController {

    private final ImageAnalysisService imageAnalysisService;

    public ImageAnalysisController(
            ImageAnalysisService imageAnalysisService) {

        this.imageAnalysisService = imageAnalysisService;
    }

    @PostMapping("/analyze-image")
    public ResponseEntity<Map<String, Object>> analyzeImage(
            @RequestParam("image") MultipartFile image) {

        return ResponseEntity.ok(
                imageAnalysisService.analyzeImage(image)
        );
    }
}
