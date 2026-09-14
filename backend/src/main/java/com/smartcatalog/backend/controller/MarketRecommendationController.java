package com.smartcatalog.backend.controller;

import com.smartcatalog.backend.ai.MarketRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class MarketRecommendationController {

    private final MarketRecommendationService marketRecommendationService;

    public MarketRecommendationController(
            MarketRecommendationService marketRecommendationService) {

        this.marketRecommendationService = marketRecommendationService;
    }

    @PostMapping("/recommend-markets")
    public ResponseEntity<List<Map<String, Object>>> recommendMarkets(
            @RequestParam String material,
            @RequestParam String category,
            @RequestParam String type) {

        return ResponseEntity.ok(
                marketRecommendationService.recommendMarkets(
                        material,
                        category,
                        type
                )
        );
    }
}