package com.smartcatalog.backend.controller;

import com.smartcatalog.backend.entity.Market;
import com.smartcatalog.backend.entity.MarketProduct;
import com.smartcatalog.backend.entity.Product;
import com.smartcatalog.backend.repository.MarketRepository;
import com.smartcatalog.backend.repository.ProductRepository;
import com.smartcatalog.backend.service.MarketProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/market-products")
public class MarketProductController {

    private final MarketProductService marketProductService;
    private final MarketRepository marketRepository;
    private final ProductRepository productRepository;

    public MarketProductController(
            MarketProductService marketProductService,
            MarketRepository marketRepository,
            ProductRepository productRepository) {
        this.marketProductService = marketProductService;
        this.marketRepository = marketRepository;
        this.productRepository = productRepository;
    }

    @PostMapping
    public ResponseEntity<MarketProduct> createMarketProduct(
            @RequestBody MarketProduct marketProduct,
            @RequestParam Long marketId,
            @RequestParam Long productId) {

        Market market = marketRepository.findById(marketId).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);

        if (market == null || product == null) {
            return ResponseEntity.notFound().build();
        }

        marketProduct.setMarket(market);
        marketProduct.setProduct(product);

        return ResponseEntity.ok(
                marketProductService.createMarketProduct(marketProduct)
        );
    }

    @GetMapping
    public List<MarketProduct> getAllMarketProducts() {
        return marketProductService.getAllMarketProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarketProduct> getMarketProductById(
            @PathVariable Long id) {

        MarketProduct marketProduct =
                marketProductService.getMarketProductById(id);

        if (marketProduct == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(marketProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MarketProduct> updateMarketProduct(
            @PathVariable Long id,
            @RequestBody MarketProduct marketProduct) {

        MarketProduct updated =
                marketProductService.updateMarketProduct(id, marketProduct);

        if (updated == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMarketProduct(
            @PathVariable Long id) {

        boolean deleted =
                marketProductService.deleteMarketProduct(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                "Market product deleted successfully"
        );
    }
}