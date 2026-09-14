package com.smartcatalog.backend.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
public class SpeechToTextService {

    public Map<String, Object> convertSpeech(MultipartFile audio) {

        Map<String, Object> result = new HashMap<>();

        result.put("fileName", audio.getOriginalFilename());
        result.put("fileType", audio.getContentType());
        result.put("fileSize", audio.getSize());

        result.put("language", "unknown");
        result.put("text", "Speech transcription will be generated here.");
        result.put("confidenceScore", 0.75);

        return result;
    }
}
