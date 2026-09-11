package com.smartcatalog.backend.controller;

import com.smartcatalog.backend.ai.AIAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIAnalysisController {

    private final AIAnalysisService aiAnalysisService;

    public AIAnalysisController(AIAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeProduct(
            @RequestParam String productName,
            @RequestParam String material,
            @RequestParam String category,
            @RequestParam String type,
            @RequestParam String description) {

        return ResponseEntity.ok(
                aiAnalysisService.analyzeProduct(
                        productName,
                        material,
                        category,
                        type,
                        description
                )
        );
    }
}