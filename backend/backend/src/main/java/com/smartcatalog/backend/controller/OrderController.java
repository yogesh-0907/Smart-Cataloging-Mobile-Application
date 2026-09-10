package com.smartcatalog.backend.controller;

import com.smartcatalog.backend.entity.Order;
import com.smartcatalog.backend.entity.Product;
import com.smartcatalog.backend.repository.ProductRepository;
import com.smartcatalog.backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final ProductRepository productRepository;

    public OrderController(
            OrderService orderService,
            ProductRepository productRepository) {
        this.orderService = orderService;
        this.productRepository = productRepository;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestBody @jakarta.validation.Valid Order order,
            @RequestParam Long productId) {

        Product product = productRepository.findById(productId).orElse(null);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        order.setProduct(product);

        return ResponseEntity.ok(
                orderService.createOrder(order)
        );
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {

        Order order = orderService.getOrderById(id);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(
            @PathVariable Long id,
            @RequestBody @jakarta.validation.Valid Order order) {

        Order updatedOrder = orderService.updateOrder(id, order);

        if (updatedOrder == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrder(@PathVariable Long id) {

        boolean deleted = orderService.deleteOrder(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok("Order deleted successfully");
    }
}