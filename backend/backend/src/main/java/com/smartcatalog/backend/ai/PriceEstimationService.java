package com.smartcatalog.backend.ai;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PriceEstimationService {

    public Map<String, Object> estimatePrice(
            String material,
            String category,
            String type) {

        Map<String, Object> result = new HashMap<>();

        double estimatedPrice = 500.0;

        if (material != null) {
            if (material.equalsIgnoreCase("Bamboo")) {
                estimatedPrice += 300;
            } else if (material.equalsIgnoreCase("Cane")) {
                estimatedPrice += 250;
            } else if (material.equalsIgnoreCase("Wood")) {
                estimatedPrice += 500;
            }
        }

        if (category != null && !category.isBlank()) {
            estimatedPrice += 100;
        }

        if (type != null && !type.isBlank()) {
            estimatedPrice += 100;
        }

        result.put("material", material);
        result.put("category", category);
        result.put("type", type);
        result.put("estimatedPrice", estimatedPrice);
        result.put("currency", "INR");
        result.put("confidenceScore", 0.75);

        return result;
    }
}