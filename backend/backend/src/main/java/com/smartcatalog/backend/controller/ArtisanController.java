package com.smartcatalog.backend.controller;

import com.smartcatalog.backend.entity.Artisan;
import com.smartcatalog.backend.service.ArtisanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/artisans")
public class ArtisanController {

    private final ArtisanService artisanService;

    public ArtisanController(ArtisanService artisanService) {
        this.artisanService = artisanService;
    }

    @PostMapping
    public Artisan createArtisan(@RequestBody Artisan artisan) {
        return artisanService.createArtisan(artisan);
    }

    @GetMapping
    public List<Artisan> getAllArtisans() {
        return artisanService.getAllArtisans();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Artisan> getArtisanById(@PathVariable Long id) {

        Artisan artisan = artisanService.getArtisanById(id);

        if (artisan == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(artisan);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Artisan> updateArtisan(
            @PathVariable Long id,
            @RequestBody Artisan artisan) {

        Artisan updatedArtisan = artisanService.updateArtisan(id, artisan);

        if (updatedArtisan == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedArtisan);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteArtisan(@PathVariable Long id) {

        boolean deleted = artisanService.deleteArtisan(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok("Artisan deleted successfully");
    }
}