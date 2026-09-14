package com.smartcatalog.backend;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class PythonAIService {

    public String testConnection() throws Exception {

        URL url = new URL("http://127.0.0.1:8000/docs");

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");

        try (InputStream inputStream = connection.getInputStream()) {
            return new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }
    }

    public String analyzeProductImage(MultipartFile image)
            throws Exception {

        String boundary =
                "----WebKitFormBoundary" + UUID.randomUUID();

        URL url = new URL(
                "http://127.0.0.1:8000/api/analyze-product"
        );

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        connection.setRequestProperty(
                "Content-Type",
                "multipart/form-data; boundary=" + boundary
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        String filename = image.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            filename = "image.jpg";
        }

        String contentType = image.getContentType();

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        byte[] imageBytes = image.getBytes();

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        output.write(
                ("--" + boundary + "\r\n")
                        .getBytes(StandardCharsets.UTF_8)
        );

        output.write(
                ("Content-Disposition: form-data; name=\"image\"; filename=\"" +
                        filename + "\"\r\n")
                        .getBytes(StandardCharsets.UTF_8)
        );

        output.write(
                ("Content-Type: " + contentType + "\r\n")
                        .getBytes(StandardCharsets.UTF_8)
        );

        output.write(
                "\r\n".getBytes(StandardCharsets.UTF_8)
        );

        output.write(imageBytes);

        output.write(
                ("\r\n--" + boundary + "--\r\n")
                        .getBytes(StandardCharsets.UTF_8)
        );

        byte[] requestBody = output.toByteArray();

        connection.setFixedLengthStreamingMode(
                requestBody.length
        );

        connection.getOutputStream().write(requestBody);
        connection.getOutputStream().flush();
        connection.getOutputStream().close();

        int statusCode = connection.getResponseCode();

        InputStream responseStream;

        if (statusCode >= 400) {
            responseStream = connection.getErrorStream();
        } else {
            responseStream = connection.getInputStream();
        }

        String response;

        try (InputStream stream = responseStream) {

            if (stream == null) {
                response = "";
            } else {
                response = new String(
                        stream.readAllBytes(),
                        StandardCharsets.UTF_8
                );
            }
        }

        if (statusCode >= 400) {
            throw new RuntimeException(
                    "Python AI returned HTTP " +
                    statusCode +
                    ": " +
                    response
            );
        }

        return response;
    }
}