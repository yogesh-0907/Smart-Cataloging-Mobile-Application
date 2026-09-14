package com.smartcatalog.backend.service;

import com.smartcatalog.backend.entity.Artisan;
import com.smartcatalog.backend.repository.ArtisanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArtisanService {

    private final ArtisanRepository artisanRepository;

    public ArtisanService(ArtisanRepository artisanRepository) {
        this.artisanRepository = artisanRepository;
    }

    public Artisan createArtisan(Artisan artisan) {
        return artisanRepository.save(artisan);
    }

    public List<Artisan> getAllArtisans() {
        return artisanRepository.findAll();
    }

    public Artisan getArtisanById(Long id) {
        return artisanRepository.findById(id).orElse(null);
    }

    public Artisan updateArtisan(Long id, Artisan artisan) {
        Artisan existingArtisan = artisanRepository.findById(id).orElse(null);

        if (existingArtisan == null) {
            return null;
        }

        existingArtisan.setName(artisan.getName());
        existingArtisan.setLocation(artisan.getLocation());
        existingArtisan.setContact(artisan.getContact());
        existingArtisan.setAge(artisan.getAge());
        existingArtisan.setExperience(artisan.getExperience());

        return artisanRepository.save(existingArtisan);
    }

    public boolean deleteArtisan(Long id) {
        if (!artisanRepository.existsById(id)) {
            return false;
        }

        artisanRepository.deleteById(id);
        return true;
    }
}