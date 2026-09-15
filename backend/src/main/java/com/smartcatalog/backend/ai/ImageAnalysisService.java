package com.smartcatalog.backend.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageAnalysisService {

    private final ObjectMapper objectMapper;

    @Value("${python.ai.base-url:http://127.0.0.1:8000}")
    private String pythonAiBaseUrl;

    public ImageAnalysisService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> analyzeImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image file is required.");
        }

        try {
            String originalFilename = image.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                originalFilename = "image";
            }

            MediaType imageContentType;
            try {
                imageContentType = image.getContentType() == null
                        ? MediaType.APPLICATION_OCTET_STREAM
                        : MediaType.parseMediaType(image.getContentType());
            } catch (IllegalArgumentException e) {
                imageContentType = MediaType.APPLICATION_OCTET_STREAM;
            }

            String boundary = "----SmartCatalog" + UUID.randomUUID();
            byte[] requestBody = createMultipartBody(
                    boundary, "image", originalFilename, imageContentType, image.getBytes());

            HttpURLConnection connection = (HttpURLConnection) new URL(
                    pythonAiBaseUrl + "/api/analyze-product").openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);
            connection.setRequestProperty("Accept", "application/json");
            connection.setFixedLengthStreamingMode(requestBody.length);

            try (OutputStream output = connection.getOutputStream()) {
                output.write(requestBody);
            }

            int statusCode = connection.getResponseCode();
            InputStream responseStream = statusCode >= 400
                    ? connection.getErrorStream() : connection.getInputStream();
            String response = responseStream == null ? "" : new String(
                    responseStream.readAllBytes(), StandardCharsets.UTF_8);

            if (statusCode >= 400) {
                throw new IOException("Python AI returned HTTP " + statusCode + ": " + response);
            }

            return objectMapper.readValue(response, Map.class);
        } catch (IOException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Unable to read uploaded image.", e);
        }
    }

    private byte[] createMultipartBody(
            String boundary,
            String fieldName,
            String filename,
            MediaType contentType,
            byte[] fileBytes) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"" + fieldName
                + "\"; filename=\"" + filename + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Type: " + contentType + "\r\n\r\n")
                .getBytes(StandardCharsets.UTF_8));
        output.write(fileBytes);
        output.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return output.toByteArray();
    }
}
