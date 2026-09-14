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
    public ResponseEntity<Map<String, Object>> createUser(
            @RequestBody @jakarta.validation.Valid User user) {

        User createdUser = userService.createUser(user);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", createdUser.getUserId());
        response.put("name", createdUser.getName());
        response.put("mobileNumber", createdUser.getMobileNumber());
        response.put("role", createdUser.getRole());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {

        List<Map<String, Object>> response = userService.getAllUsers()
                .stream()
                .map(this::safeUserResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(
            @PathVariable Long id) {

        User user = userService.getUserById(id);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(safeUserResponse(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @RequestBody @jakarta.validation.Valid User user) {

        User updatedUser = userService.updateUser(id, user);

        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(safeUserResponse(updatedUser));
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

    private Map<String, Object> safeUserResponse(User user) {

        Map<String, Object> response = new HashMap<>();

        response.put("userId", user.getUserId());
        response.put("name", user.getName());
        response.put("mobileNumber", user.getMobileNumber());
        response.put("role", user.getRole());

        return response;
    }
}