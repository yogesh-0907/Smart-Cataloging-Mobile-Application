package com.smartcatalog.backend.ai;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIProductIntegrationService {

    private final AIAnalysisService aiAnalysisService;
    private final PriceEstimationService priceEstimationService;
    private final ProductFeatureService productFeatureService;

    public AIProductIntegrationService(
            AIAnalysisService aiAnalysisService,
            PriceEstimationService priceEstimationService,
            ProductFeatureService productFeatureService) {

        this.aiAnalysisService = aiAnalysisService;
        this.priceEstimationService = priceEstimationService;
        this.productFeatureService = productFeatureService;
    }

    public Map<String, Object> analyzeProduct(
            String productName,
            String material,
            String category,
            String type,
            String description) {

        Map<String, Object> result = new HashMap<>();

        result.put("analysis",
                aiAnalysisService.analyzeProduct(
                        productName,
                        material,
                        category,
                        type,
                        description
                ));

        result.put("priceEstimate",
                priceEstimationService.estimatePrice(
                        material,
                        category,
                        type
                ));

        result.put("features",
                productFeatureService.extractFeatures(
                        material,
                        category,
                        type,
                        description
                ));

        return result;
    }
}