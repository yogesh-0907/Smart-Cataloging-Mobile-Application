package com.smartcatalog.backend.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class SpeechToTextService {

    private final RestClient restClient;

    @Value("${python.ai.base-url:http://127.0.0.1:8000}")
    private String pythonAiBaseUrl;

    public SpeechToTextService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public Map<String, Object> convertSpeech(MultipartFile audio) {

        if (audio == null || audio.isEmpty()) {
            throw new IllegalArgumentException("Audio file is required.");
        }

        try {
            String filename = audio.getOriginalFilename() != null
                    ? audio.getOriginalFilename()
                    : "audio.mp3";

            String contentType = audio.getContentType() != null
                    ? audio.getContentType()
                    : "application/octet-stream";

            byte[] audioBytes = audio.getBytes();

            if (audioBytes.length == 0) {
                throw new IllegalArgumentException("Audio file contains no data.");
            }

            String audioBase64 =
                    Base64.getEncoder().encodeToString(audioBytes);

            Map<String, Object> request = new HashMap<>();
            request.put("filename", filename);
            request.put("content_type", contentType);
            request.put("audio_base64", audioBase64);

            return restClient
                    .post()
                    .uri(pythonAiBaseUrl + "/api/transcribe-voice-json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(Map.class);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read audio file.", e);
        } catch (RestClientException e) {
            throw new RuntimeException(
                    "Python AI speech service failed: " + e.getMessage(), e);
        }
    }
}