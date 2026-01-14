package com.cymelle.backend.controller;

import com.cymelle.backend.dto.OrderRequest;
import com.cymelle.backend.model.Order;
import com.cymelle.backend.model.OrderStatus;
import com.cymelle.backend.model.User;
import com.cymelle.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    public ResponseEntity<Order> placeOrder(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid OrderRequest request
    ) {
        return ResponseEntity.ok(service.placeOrder(user, request));
    }

    @GetMapping
    public ResponseEntity<Page<Order>> getOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable
    ) {
        // If status is provided, search by status (maybe admin only feature in real world, but req implies search capability)
        // Check requirement: "search for rides/orders (by user or status)"
        // If user is ADMIN, maybe they can search all? 
        // For simplicity, if status provided we search by status, else by user.
        if (status != null) {
            return ResponseEntity.ok(service.searchOrdersByStatus(status, pageable));
        }
        return ResponseEntity.ok(service.getOrdersByUser(user, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOrderById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status
    ) {
        return ResponseEntity.ok(service.updateOrderStatus(id, status));
    }
}
