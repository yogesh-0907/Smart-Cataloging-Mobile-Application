package com.smartcatalog.backend.controller;

import com.smartcatalog.backend.ai.ImageProductCreationService;
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
    public ResponseEntity<?> createProductFromImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam Long artisanId,
            @RequestParam String productName,
            @RequestParam Double price) {

        ImageProductCreationService.ProductCreationResult result =
                imageProductCreationService.createProductFromImage(
                        image,
                        artisanId,
                        productName,
                        price
                );

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }
}
