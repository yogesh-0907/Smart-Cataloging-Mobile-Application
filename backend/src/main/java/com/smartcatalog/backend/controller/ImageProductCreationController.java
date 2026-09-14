package com.smartcatalog.backend.controller;

import com.smartcatalog.backend.ai.ImageProductCreationService;
import com.smartcatalog.backend.entity.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai")
public class ImageProductCreationController {

    private final ImageProductCreationService imageProductCreationService;

    public ImageProductCreationController(
            ImageProductCreationService imageProductCreationService) {

        this.imageProductCreationService = imageProductCreationService;
    }

    @PostMapping("/create-product-from-image")
    public ResponseEntity<Product> createProductFromImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam Long artisanId,
            @RequestParam String productName,
            @RequestParam Double price) {

        Product product =
                imageProductCreationService.createProductFromImage(
                        image,
                        artisanId,
                        productName,
                        price
                );

        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(product);
    }
}