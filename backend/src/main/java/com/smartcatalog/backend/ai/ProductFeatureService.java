package com.smartcatalog.backend.ai;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductFeatureService {

    public Map<String, Object> extractFeatures(
            String material,
            String category,
            String type,
            String description) {

        Map<String, Object> result = new HashMap<>();
        List<String> features = new ArrayList<>();

        if (material != null && !material.isBlank()) {
            features.add("Handcrafted " + material);
        }

        if (category != null && !category.isBlank()) {
            features.add(category + " product");
        }

        if (type != null && !type.isBlank()) {
            features.add(type);
        }

        if (description != null && !description.isBlank()) {
            features.add("Traditional design");
        }

        result.put("material", material);
        result.put("category", category);
        result.put("type", type);
        result.put("features", features);
        result.put("keywords", List.of(
                material,
                category,
                type,
                "handmade",
                "traditional",
                "artisan"
        ));
        result.put("confidenceScore", 0.80);

        return result;
    }
}