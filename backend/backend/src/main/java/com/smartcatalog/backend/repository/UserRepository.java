package com.smartcatalog.backend.repository;

import com.smartcatalog.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}