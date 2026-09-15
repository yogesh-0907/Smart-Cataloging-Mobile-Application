package com.smartcatalog.backend.ai;

import com.smartcatalog.backend.entity.Artisan;
import com.smartcatalog.backend.entity.Product;
import com.smartcatalog.backend.repository.ArtisanRepository;
import com.smartcatalog.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DemoProductService {

    private final ImageAnalysisService imageAnalysisService;
    private final SpeechToTextService speechToTextService;
    private final PriceEstimationService priceEstimationService;
    private final MarketRecommendationService marketRecommendationService;
    private final ArtisanRepository artisanRepository;
    private final ProductRepository productRepository;

    public DemoProductService(
            ImageAnalysisService imageAnalysisService,
            SpeechToTextService speechToTextService,
            PriceEstimationService priceEstimationService,
            MarketRecommendationService marketRecommendationService,
            ArtisanRepository artisanRepository,
            ProductRepository productRepository) {
        this.imageAnalysisService = imageAnalysisService;
        this.speechToTextService = speechToTextService;
        this.priceEstimationService = priceEstimationService;
        this.marketRecommendationService = marketRecommendationService;
        this.artisanRepository = artisanRepository;
        this.productRepository = productRepository;
    }

    public Map<String, Object> createProduct(
            MultipartFile image,
            MultipartFile audio,
            Long artisanId,
            String requestedProductName) {

        Artisan artisan = artisanRepository.findById(artisanId)
                .orElseThrow(() -> new IllegalArgumentException("Artisan not found."));

        Map<String, Object> imageAnalysis = imageAnalysisService.analyzeImage(image);
        Map<String, Object> voiceData = null;
        String voiceError = null;

        if (audio != null && !audio.isEmpty()) {
            try {
                voiceData = speechToTextService.createVoiceCatalog(audio);
            } catch (RuntimeException error) {
                voiceError = "Voice cataloging was unavailable; the image-based listing was created.";
            }
        }

        Map<String, Object> voiceCatalog = nestedMap(voiceData, "catalog");
        String material = text(firstPresent(voiceCatalog, imageAnalysis, "material"));
        String category = text(firstPresent(voiceCatalog, imageAnalysis, "category"));
        String type = text(firstPresent(voiceCatalog, imageAnalysis, "product_type"));
        String description = text(firstPresent(voiceCatalog, imageAnalysis, "description"));
        if (description.isBlank()) {
            description = text(imageAnalysis.get("description_en"));
        }

        String productName = requestedProductName == null || requestedProductName.isBlank()
                ? text(voiceCatalog.get("product_name")) : requestedProductName.trim();
        if (productName.isBlank()) {
            productName = type.isBlank() ? "Untitled Product" : type;
        }

        Product product = new Product();
        product.setProductName(productName);
        product.setMaterial(nonBlank(material, "Unknown"));
        product.setCategory(nonBlank(category, "Uncategorized"));
        product.setType(nonBlank(type, "Product"));
        product.setDescription(nonBlank(description, "AI-generated product listing."));
        product.setPrice(0.0);
        product.setArtisan(artisan);
        product = productRepository.save(product);

        Map<String, Object> price = null;
        List<Map<String, Object>> markets = List.of();
        Map<String, String> integrationWarnings = new LinkedHashMap<>();
        if (voiceError != null) {
            integrationWarnings.put("voice", voiceError);
        }

        try {
            price = priceEstimationService.estimatePrice(
                    product.getMaterial(), product.getCategory(), product.getType());
        } catch (RuntimeException error) {
            integrationWarnings.put("pricing", "Pricing is temporarily unavailable.");
        }

        try {
            markets = marketRecommendationService.recommendMarkets(
                    product.getMaterial(), product.getCategory(), product.getType());
        } catch (RuntimeException error) {
            integrationWarnings.put("markets", "Market recommendations are temporarily unavailable.");
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("product", productDetails(product));
        response.put("image_analysis", imageAnalysis);
        if (voiceData != null) {
            response.put("voice_catalog", voiceData);
        }
        response.put("suggested_price", price);
        response.put("recommended_markets", markets);
        response.put("listing_status", integrationWarnings.isEmpty()
                ? "created" : "created_with_ai_warnings");
        if (!integrationWarnings.isEmpty()) {
            response.put("integration_warnings", integrationWarnings);
        }
        return response;
    }

    private Map<String, Object> productDetails(Product product) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("product_id", product.getProductId());
        details.put("artisan_id", product.getArtisan().getArtisanId());
        details.put("product_name", product.getProductName());
        details.put("material", product.getMaterial());
        details.put("category", product.getCategory());
        details.put("type", product.getType());
        details.put("description", product.getDescription());
        details.put("price", product.getPrice());
        return details;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> nestedMap(Map<String, Object> source, String key) {
        if (source != null && source.get(key) instanceof Map<?, ?> value) {
            return (Map<String, Object>) value;
        }
        return Map.of();
    }

    private Object firstPresent(Map<String, Object> preferred, Map<String, Object> fallback, String key) {
        Object value = preferred.get(key);
        return value == null || text(value).isBlank() ? fallback.get(key) : value;
    }

    private String text(Object value) {
        if (value instanceof List<?> values) {
            return values.stream().map(String::valueOf).reduce((left, right) -> left + ", " + right)
                    .orElse("");
        }
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
