package com.smartcatalog.backend.repository;

import com.smartcatalog.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}