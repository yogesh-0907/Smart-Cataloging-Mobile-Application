package com.smartcatalog.backend.ai;

import com.smartcatalog.backend.entity.Artisan;
import com.smartcatalog.backend.entity.Product;
import com.smartcatalog.backend.repository.ArtisanRepository;
import com.smartcatalog.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public Product createProductFromImage(
            MultipartFile image,
            Long artisanId,
            String productName,
            Double price) {

        Map<String, Object> analysis =
                imageAnalysisService.analyzeImage(image);

        Artisan artisan =
                artisanRepository.findById(artisanId).orElse(null);

        if (artisan == null) {
            return null;
        }

        Product product = new Product();

        product.setProductName(productName);
        product.setMaterial((String) analysis.get("material"));
        product.setCategory((String) analysis.get("category"));
        product.setType((String) analysis.get("productType"));
        product.setDescription((String) analysis.get("aiDescription"));
        product.setPrice(price);
        product.setArtisan(artisan);

        return productRepository.save(product);
    }
}