package com.smartcatalog.backend.ai;

import com.smartcatalog.backend.entity.Market;
import com.smartcatalog.backend.repository.MarketRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MarketRecommendationService {

    private final MarketRepository marketRepository;

    public MarketRecommendationService(MarketRepository marketRepository) {
        this.marketRepository = marketRepository;
    }

    public List<Map<String, Object>> recommendMarkets(
            String material,
            String category,
            String type) {

        List<Market> markets = marketRepository.findAll();
        List<Map<String, Object>> recommendations = new ArrayList<>();

        for (Market market : markets) {

            double score = 50.0;

            if (market.getType() != null) {
                if (market.getType().equalsIgnoreCase("RETAILER")) {
                    score += 20;
                } else if (market.getType().equalsIgnoreCase("WHOLESALER")) {
                    score += 15;
                }
            }

            if (category != null && !category.isBlank()) {
                score += 10;
            }

            if (material != null && !material.isBlank()) {
                score += 10;
            }

            if (type != null && !type.isBlank()) {
                score += 10;
            }

            if (score > 100) {
                score = 100;
            }

            Map<String, Object> result = new HashMap<>();

            result.put("marketId", market.getMarketId());
            result.put("marketName", market.getName());
            result.put("location", market.getLocation());
            result.put("type", market.getType());
            result.put("matchScore", score);

            recommendations.add(result);
        }

        recommendations.sort(
                (a, b) -> Double.compare(
                        (Double) b.get("matchScore"),
                        (Double) a.get("matchScore")
                )
        );

        return recommendations;
    }
}
