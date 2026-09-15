package com.smartcatalog.backend.controller;

import com.smartcatalog.backend.ai.DemoProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class DemoProductController {

    private final DemoProductService demoProductService;

    public DemoProductController(DemoProductService demoProductService) {
        this.demoProductService = demoProductService;
    }

    @PostMapping("/create-product")
    public ResponseEntity<Map<String, Object>> createProduct(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "audio", required = false) MultipartFile audio,
            @RequestParam Long artisanId,
            @RequestParam(value = "productName", required = false) String productName) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    demoProductService.createProduct(image, audio, artisanId, productName));
        } catch (IllegalArgumentException error) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("listing_status", "not_created");
            response.put("message", error.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (RuntimeException error) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("listing_status", "not_created");
            response.put("message", "Image analysis is temporarily unavailable. No product was created.");
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
        }
    }
}
