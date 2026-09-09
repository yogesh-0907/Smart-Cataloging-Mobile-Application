package com.smartcatalog.backend.repository;

import com.smartcatalog.backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}