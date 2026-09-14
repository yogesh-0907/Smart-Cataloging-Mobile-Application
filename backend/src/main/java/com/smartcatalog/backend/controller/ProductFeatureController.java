package com.smartcatalog.backend.controller;

import com.smartcatalog.backend.ai.ProductFeatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class ProductFeatureController {

    private final ProductFeatureService productFeatureService;

    public ProductFeatureController(
            ProductFeatureService productFeatureService) {

        this.productFeatureService = productFeatureService;
    }

    @PostMapping("/extract-features")
    public ResponseEntity<Map<String, Object>> extractFeatures(
            @RequestParam String material,
            @RequestParam String category,
            @RequestParam String type,
            @RequestParam String description) {

        return ResponseEntity.ok(
                productFeatureService.extractFeatures(
                        material,
                        category,
                        type,
                        description
                )
        );
    }
}