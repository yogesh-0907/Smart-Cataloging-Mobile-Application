package com.smartcatalog.backend.controller;

import com.smartcatalog.backend.entity.Artisan;
import com.smartcatalog.backend.entity.Product;
import com.smartcatalog.backend.repository.ArtisanRepository;
import com.smartcatalog.backend.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ArtisanRepository artisanRepository;

    public ProductController(ProductService productService, ArtisanRepository artisanRepository) {
        this.productService = productService;
        this.artisanRepository = artisanRepository;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestBody Product product,
            @RequestParam Long artisanId) {

        Artisan artisan = artisanRepository.findById(artisanId).orElse(null);

        if (artisan == null) {
            return ResponseEntity.notFound().build();
        }

        product.setArtisan(artisan);

        return ResponseEntity.ok(productService.createProduct(product));
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {

        Product product = productService.getProductById(id);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product) {

        Product updatedProduct = productService.updateProduct(id, product);

        if (updatedProduct == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {

        boolean deleted = productService.deleteProduct(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok("Product deleted successfully");
    }
}