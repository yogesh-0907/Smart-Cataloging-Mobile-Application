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
            @RequestParam String type,
            @RequestParam(defaultValue = "0") double materialCost,
            @RequestParam(defaultValue = "0") double laborHours,
            @RequestParam(defaultValue = "0") double laborRate,
            @RequestParam(defaultValue = "0") double packagingCost,
            @RequestParam(defaultValue = "10") double overheadPercent,
            @RequestParam(defaultValue = "20") double profitMarginPercent) {

        return ResponseEntity.ok(
                priceEstimationService.estimatePrice(
                        material,
                        category,
                        type,
                        materialCost,
                        laborHours,
                        laborRate,
                        packagingCost,
                        overheadPercent,
                        profitMarginPercent
                )
        );
    }
}
