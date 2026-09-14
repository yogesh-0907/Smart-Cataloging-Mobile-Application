package com.smartcatalog.backend.service;

import com.smartcatalog.backend.ai.AIProductIntegrationService;
import com.smartcatalog.backend.entity.Product;
import com.smartcatalog.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final AIProductIntegrationService aiProductIntegrationService;

    public ProductService(
            ProductRepository productRepository,
            AIProductIntegrationService aiProductIntegrationService) {

        this.productRepository = productRepository;
        this.aiProductIntegrationService = aiProductIntegrationService;
    }

    public Product createProduct(Product product) {

        Map<String, Object> aiResult =
                aiProductIntegrationService.analyzeProduct(
                        product.getProductName(),
                        product.getMaterial(),
                        product.getCategory(),
                        product.getType(),
                        product.getDescription()
                );

        Map<String, Object> analysis =
                (Map<String, Object>) aiResult.get("analysis");

        if (product.getDescription() == null ||
                product.getDescription().isBlank()) {

            product.setDescription(
                    (String) analysis.get("aiDescription")
            );
        }

        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product updateProduct(Long id, Product product) {

        Product existingProduct =
                productRepository.findById(id).orElse(null);

        if (existingProduct == null) {
            return null;
        }

        existingProduct.setProductName(product.getProductName());
        existingProduct.setMaterial(product.getMaterial());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setType(product.getType());
        existingProduct.setDescription(product.getDescription());

        return productRepository.save(existingProduct);
    }

    public boolean deleteProduct(Long id) {

        if (!productRepository.existsById(id)) {
            return false;
        }

        productRepository.deleteById(id);
        return true;
    }

    public List<Product> searchByName(String name) {
        return productRepository.findByProductNameContainingIgnoreCase(name);
    }

    public List<Product> searchByCategory(String category) {
        return productRepository.findByCategoryContainingIgnoreCase(category);
    }

    public List<Product> searchByMaterial(String material) {
        return productRepository.findByMaterialContainingIgnoreCase(material);
    }

    public List<Product> searchByType(String type) {
        return productRepository.findByTypeContainingIgnoreCase(type);
    }
}