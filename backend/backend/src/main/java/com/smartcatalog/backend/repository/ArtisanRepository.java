package com.smartcatalog.backend.repository;

import com.smartcatalog.backend.entity.Artisan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtisanRepository extends JpaRepository<Artisan, Long> {
}