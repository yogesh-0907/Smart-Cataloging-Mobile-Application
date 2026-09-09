package com.smartcatalog.backend.service;

import com.smartcatalog.backend.entity.Market;
import com.smartcatalog.backend.repository.MarketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketService {

    private final MarketRepository marketRepository;

    public MarketService(MarketRepository marketRepository) {
        this.marketRepository = marketRepository;
    }

    public Market createMarket(Market market) {
        return marketRepository.save(market);
    }

    public List<Market> getAllMarkets() {
        return marketRepository.findAll();
    }

    public Market getMarketById(Long id) {
        return marketRepository.findById(id).orElse(null);
    }

    public Market updateMarket(Long id, Market market) {
        Market existingMarket = marketRepository.findById(id).orElse(null);

        if (existingMarket == null) {
            return null;
        }

        existingMarket.setName(market.getName());
        existingMarket.setLocation(market.getLocation());
        existingMarket.setContact(market.getContact());
        existingMarket.setType(market.getType());

        return marketRepository.save(existingMarket);
    }

    public boolean deleteMarket(Long id) {
        if (!marketRepository.existsById(id)) {
            return false;
        }

        marketRepository.deleteById(id);
        return true;
    }
}