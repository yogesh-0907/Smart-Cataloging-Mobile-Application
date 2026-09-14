package com.smartcatalog.backend.controller;

import com.smartcatalog.backend.entity.Market;
import com.smartcatalog.backend.service.MarketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/markets")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @PostMapping
    public Market createMarket(@RequestBody @jakarta.validation.Valid Market market) {
        return marketService.createMarket(market);
    }

    @GetMapping
    public List<Market> getAllMarkets() {
        return marketService.getAllMarkets();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Market> getMarketById(@PathVariable Long id) {

        Market market = marketService.getMarketById(id);

        if (market == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(market);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Market> updateMarket(
            @PathVariable Long id,
            @RequestBody @jakarta.validation.Valid Market market) {

        Market updatedMarket = marketService.updateMarket(id, market);

        if (updatedMarket == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedMarket);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMarket(@PathVariable Long id) {

        boolean deleted = marketService.deleteMarket(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok("Market deleted successfully");
    }
}