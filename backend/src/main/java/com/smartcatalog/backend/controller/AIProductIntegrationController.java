package com.smartcatalog.backend.controller;

import com.smartcatalog.backend.ai.AIProductIntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIProductIntegrationController {

    private final AIProductIntegrationService aiProductIntegrationService;

    public AIProductIntegrationController(
            AIProductIntegrationService aiProductIntegrationService) {

        this.aiProductIntegrationService = aiProductIntegrationService;
    }

    @PostMapping("/analyze-product")
    public ResponseEntity<Map<String, Object>> analyzeProduct(
            @RequestParam String productName,
            @RequestParam String material,
            @RequestParam String category,
            @RequestParam String type,
            @RequestParam String description) {

        return ResponseEntity.ok(
                aiProductIntegrationService.analyzeProduct(
                        productName,
                        material,
                        category,
                        type,
                        description
                )
        );
    }
}