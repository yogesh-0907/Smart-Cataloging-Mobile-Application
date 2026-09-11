package com.smartcatalog.backend.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
public class ImageAnalysisService {

    public Map<String, Object> analyzeImage(MultipartFile image) {

        Map<String, Object> result = new HashMap<>();

        result.put("fileName", image.getOriginalFilename());
        result.put("fileType", image.getContentType());
        result.put("fileSize", image.getSize());

        result.put("category", "Handicraft");
        result.put("productType", "Handmade Product");
        result.put("material", "Bamboo");
        result.put("confidenceScore", 0.80);

        result.put(
                "aiDescription",
                "AI detected a handcrafted traditional handicraft product."
        );

        return result;
    }
}