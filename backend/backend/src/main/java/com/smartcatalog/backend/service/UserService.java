package com.smartcatalog.backend.service;

import com.smartcatalog.backend.entity.User;
import com.smartcatalog.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User updateUser(Long id, User user) {
        User existingUser = userRepository.findById(id).orElse(null);

        if (existingUser == null) {
            return null;
        }

        existingUser.setName(user.getName());
        existingUser.setMobileNumber(user.getMobileNumber());
        existingUser.setPassword(user.getPassword());
        existingUser.setRole(user.getRole());

        return userRepository.save(existingUser);
    }

    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }

        userRepository.deleteById(id);
        return true;
    }

    public User login(String mobileNumber, String password) {
        User user = userRepository.findByMobileNumber(mobileNumber).orElse(null);

        if (user == null) {
            return null;
        }

        if (!user.getPassword().equals(password)) {
            return null;
        }

        return user;
    }
}