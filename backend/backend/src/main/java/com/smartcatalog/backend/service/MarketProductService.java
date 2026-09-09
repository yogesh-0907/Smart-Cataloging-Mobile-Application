package com.smartcatalog.backend.service;

import com.smartcatalog.backend.entity.MarketProduct;
import com.smartcatalog.backend.repository.MarketProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketProductService {

    private final MarketProductRepository marketProductRepository;

    public MarketProductService(MarketProductRepository marketProductRepository) {
        this.marketProductRepository = marketProductRepository;
    }

    public MarketProduct createMarketProduct(MarketProduct marketProduct) {
        return marketProductRepository.save(marketProduct);
    }

    public List<MarketProduct> getAllMarketProducts() {
        return marketProductRepository.findAll();
    }

    public MarketProduct getMarketProductById(Long id) {
        return marketProductRepository.findById(id).orElse(null);
    }

    public MarketProduct updateMarketProduct(Long id, MarketProduct marketProduct) {
        MarketProduct existing = marketProductRepository.findById(id).orElse(null);

        if (existing == null) {
            return null;
        }

        existing.setStatus(marketProduct.getStatus());

        return marketProductRepository.save(existing);
    }

    public boolean deleteMarketProduct(Long id) {
        if (!marketProductRepository.existsById(id)) {
            return false;
        }

        marketProductRepository.deleteById(id);
        return true;
    }
}