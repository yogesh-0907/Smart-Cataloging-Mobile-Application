package com.smartcatalog.backend.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class PriceEstimationService {

    private final RestClient restClient;

    @Value("${python.ai.base-url:http://127.0.0.1:8000}")
    private String pythonAiBaseUrl;

    public PriceEstimationService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public Map<String, Object> estimatePrice(
            String material,
            String category,
            String type) {
        return estimatePrice(material, category, type, 0, 0, 0, 0, 10, 20);
    }

    public Map<String, Object> estimatePrice(
            String material,
            String category,
            String type,
            double materialCost,
            double laborHours,
            double laborRate,
            double packagingCost,
            double overheadPercent,
            double profitMarginPercent) {
        try {
            String url = UriComponentsBuilder.fromUriString(pythonAiBaseUrl)
                    .path("/api/recommend-price")
                    .queryParam("product_type", type)
                    .queryParam("material_cost", materialCost)
                    .queryParam("labor_hours", laborHours)
                    .queryParam("labor_rate", laborRate)
                    .queryParam("packaging_cost", packagingCost)
                    .queryParam("overhead_percent", overheadPercent)
                    .queryParam("profit_margin_percent", profitMarginPercent)
                    .toUriString();

            return restClient.post()
                    .uri(url)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Python AI pricing service is unavailable.", e);
        }
    }
}
