package com.smartcatalog.backend.controller;

import com.smartcatalog.backend.entity.User;
import com.smartcatalog.backend.security.JwtService;
import com.smartcatalog.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping
    public User createUser(@RequestBody @jakarta.validation.Valid User user) {
        return userService.createUser(user);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {

        User user = userService.getUserById(id);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody @jakarta.validation.Valid User user) {

        User updatedUser = userService.updateUser(id, user);

        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {

        boolean deleted = userService.deleteUser(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok("User deleted successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam String mobileNumber,
            @RequestParam String password) {

        User user = userService.login(mobileNumber, password);

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        String token = jwtService.generateToken(
                user.getUserId(),
                user.getMobileNumber(),
                user.getRole()
        );

        Map<String, Object> response = new HashMap<>();

        response.put("token", token);
        response.put("userId", user.getUserId());
        response.put("name", user.getName());
        response.put("mobileNumber", user.getMobileNumber());
        response.put("role", user.getRole());

        return ResponseEntity.ok(response);
    }
}