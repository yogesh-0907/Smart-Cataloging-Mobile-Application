package com.smartcatalog.backend.ai;

import com.smartcatalog.backend.entity.Artisan;
import com.smartcatalog.backend.entity.Product;
import com.smartcatalog.backend.repository.ArtisanRepository;
import com.smartcatalog.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImageProductCreationService {

    private final ImageAnalysisService imageAnalysisService;
    private final ProductRepository productRepository;
    private final ArtisanRepository artisanRepository;

    public ImageProductCreationService(
            ImageAnalysisService imageAnalysisService,
            ProductRepository productRepository,
            ArtisanRepository artisanRepository) {

        this.imageAnalysisService = imageAnalysisService;
        this.productRepository = productRepository;
        this.artisanRepository = artisanRepository;
    }

    public ProductCreationResult createProductFromImage(
            MultipartFile image,
            Long artisanId,
            String productName,
            Double price) {

        Map<String, Object> analysis;
        List<String> integrationWarnings = List.of();

        try {
            analysis = imageAnalysisService.analyzeImage(image);
        } catch (ResponseStatusException error) {
            if (!isAiImageAnalysisUnavailable(error)) {
                throw error;
            }

            analysis = localFallbackAnalysis(image, productName);
            integrationWarnings = List.of(
                    "AI image analysis was unavailable, so fallback catalog data was used."
            );
        }

        Artisan artisan =
                artisanRepository.findById(artisanId).orElse(null);

        if (artisan == null) {
            return null;
        }

        Product product = new Product();

        product.setProductName(productName);
        product.setMaterial(stringValue(analysis.get("material")));
        product.setCategory((String) analysis.get("category"));
        product.setType(stringValue(analysis.get("product_type")));
        product.setDescription(descriptionFor(productName, analysis));
        product.setPrice(price);
        product.setArtisan(artisan);

        return new ProductCreationResult(
                productRepository.save(product),
                integrationWarnings
        );
    }

    public record ProductCreationResult(
            Product product,
            List<String> integrationWarnings
    ) {
    }

    private String stringValue(Object value) {
        if (value instanceof List<?> values) {
            return values.stream().map(String::valueOf).reduce((a, b) -> a + ", " + b)
                    .orElse("");
        }
        return value == null ? "" : String.valueOf(value);
    }

    private String descriptionFor(String productName, Map<String, Object> analysis) {
        String description = stringValue(analysis.get("description_en")).trim();
        if (!description.isBlank()) {
            return description;
        }

        String material = stringValue(analysis.get("material")).trim();
        String type = stringValue(analysis.get("product_type")).trim();
        return productName + " - "
                + (type.isBlank() ? "product" : type)
                + (material.isBlank() ? "" : " made from " + material);
    }

    private boolean isAiImageAnalysisUnavailable(ResponseStatusException error) {
        Throwable current = error;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String normalized = message.toLowerCase();
                if (normalized.contains("resource_exhausted")
                        || normalized.contains("quota exceeded")
                        || normalized.contains("http 429")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private Map<String, Object> localFallbackAnalysis(
            MultipartFile image,
            String productName) {
        String filename = image.getOriginalFilename();
        String mimeType = image.getContentType();

        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("category", "Uncategorized");
        fallback.put("product_type", productName);
        fallback.put("material", "Unknown");
        fallback.put(
                "description_en",
                productName + " was cataloged from the uploaded image"
                        + (filename == null || filename.isBlank() ? "" : " (" + filename + ")")
                        + (mimeType == null || mimeType.isBlank() ? "" : " with MIME type " + mimeType)
                        + " while AI image analysis was unavailable."
        );
        return fallback;
    }
}
