package com.smartcatalog.backend.repository;

import com.smartcatalog.backend.entity.Market;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketRepository extends JpaRepository<Market, Long> {
}