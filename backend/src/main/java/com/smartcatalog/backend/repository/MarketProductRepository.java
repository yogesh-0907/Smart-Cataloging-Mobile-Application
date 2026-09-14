package com.smartcatalog.backend.repository;

import com.smartcatalog.backend.entity.MarketProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketProductRepository extends JpaRepository<MarketProduct, Long> {
}