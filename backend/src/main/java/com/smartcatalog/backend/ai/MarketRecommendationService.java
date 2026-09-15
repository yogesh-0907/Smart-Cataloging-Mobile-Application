package com.smartcatalog.backend.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class MarketRecommendationService {

    private final RestClient restClient;

    @Value("${python.ai.base-url:http://127.0.0.1:8000}")
    private String pythonAiBaseUrl;

    public MarketRecommendationService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public List<Map<String, Object>> recommendMarkets(
            String material,
            String category,
            String type) {

        try {
            String url = UriComponentsBuilder.fromUriString(pythonAiBaseUrl)
                    .path("/api/recommend-markets")
                    .queryParam("material", material == null ? "" : material)
                    .queryParam("category", category == null ? "" : category)
                    .queryParam("product_type", type == null ? "" : type)
                    .toUriString();

            return restClient.post()
                    .uri(url)
                    .retrieve()
                    .body(List.class);
        } catch (RestClientException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Python AI market service is unavailable.", e);
        }
    }
}
