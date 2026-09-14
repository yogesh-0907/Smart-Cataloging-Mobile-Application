package com.smartcatalog.backend.controller;

import com.smartcatalog.backend.ai.PriceEstimationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class PriceEstimationController {

    private final PriceEstimationService priceEstimationService;

    public PriceEstimationController(
            PriceEstimationService priceEstimationService) {

        this.priceEstimationService = priceEstimationService;
    }

    @PostMapping("/estimate-price")
    public ResponseEntity<Map<String, Object>> estimatePrice(
            @RequestParam String material,
            @RequestParam String category,
            @RequestParam String type) {

        return ResponseEntity.ok(
                priceEstimationService.estimatePrice(
                        material,
                        category,
                        type
                )
        );
    }
}