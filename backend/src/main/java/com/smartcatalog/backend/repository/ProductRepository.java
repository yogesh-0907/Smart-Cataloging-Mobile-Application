package com.smartcatalog.backend.repository;

import com.smartcatalog.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByProductNameContainingIgnoreCase(String productName);

    List<Product> findByCategoryContainingIgnoreCase(String category);

    List<Product> findByMaterialContainingIgnoreCase(String material);

    List<Product> findByTypeContainingIgnoreCase(String type);
}