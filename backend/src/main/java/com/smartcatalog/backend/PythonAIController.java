package com.smartcatalog.backend;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/python-ai")
public class PythonAIController {

    private final PythonAIService pythonAIService;

    public PythonAIController(PythonAIService pythonAIService) {
        this.pythonAIService = pythonAIService;
    }

    @GetMapping("/test")
    public String testPythonConnection() throws Exception {
        return pythonAIService.testConnection();
    }

    @PostMapping(
            value = "/analyze-product",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> analyzeProduct(
            @RequestPart("image") MultipartFile image)
            throws Exception {

        if (image.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("{\"error\":\"Empty image received\"}");
        }

        return ResponseEntity.ok(
                pythonAIService.analyzeProductImage(image)
        );
    }
}