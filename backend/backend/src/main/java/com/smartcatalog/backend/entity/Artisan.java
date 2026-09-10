package com.smartcatalog.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Entity
public class Artisan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long artisanId;

    @NotBlank
    private String name;

    @NotBlank
    private String location;

    @NotBlank
    private String contact;

    @Min(1)
    private int age;

    @NotBlank
    private String experience;

    @OneToMany(mappedBy = "artisan")
    private List<Product> products;

    public Artisan() {
    }

    public Artisan(String name, String location, String contact, int age, String experience) {
        this.name = name;
        this.location = location;
        this.contact = contact;
        this.age = age;
        this.experience = experience;
    }

    public Long getArtisanId() {
        return artisanId;
    }

    public void setArtisanId(Long artisanId) {
        this.artisanId = artisanId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }
}