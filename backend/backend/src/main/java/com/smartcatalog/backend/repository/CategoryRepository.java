package com.smartcatalog.backend.repository;

import com.smartcatalog.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}