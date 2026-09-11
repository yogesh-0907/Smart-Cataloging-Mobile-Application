package com.smartcatalog.backend.ai;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIAnalysisService {

    public Map<String, Object> analyzeProduct(
            String productName,
            String material,
            String category,
            String type,
            String description) {

        Map<String, Object> result = new HashMap<>();

        result.put("productName", productName);
        result.put("material", material);
        result.put("category", category);
        result.put("type", type);
        result.put("description", description);
        result.put("confidenceScore", 0.85);

        result.put(
                "aiDescription",
                "Handcrafted " + material + " " + type +
                " product suitable for traditional handicraft markets."
        );

        return result;
    }
}
