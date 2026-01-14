package com.cymelle.backend.controller;

import com.cymelle.backend.dto.OrderRequest;
import com.cymelle.backend.model.Order;
import com.cymelle.backend.model.OrderStatus;
import com.cymelle.backend.model.User;
import com.cymelle.backend.service.OrderService;
import com.cymelle.backend.dto.UpdateOrderStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.cymelle.backend.model.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Endpoints for placing and managing ecommerce orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService service;

    @PostMapping
    @Operation(
            summary = "Place a new order",
            description = "Creates a new order with multiple items. Authenticated customers only.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order placed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input or processing error"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid token")
            }
    )
    public ResponseEntity<Order> placeOrder(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid OrderRequest request
    ) {
        return ResponseEntity.ok(service.placeOrder(user, request));
    }

    @GetMapping
    @Operation(
            summary = "Get list of orders",
            description = "Retrieves a paginated list of orders. Admins can see all orders or filter by email. Customers only see their own orders."
    )
    public ResponseEntity<Page<Order>> getOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) OrderStatus status,
            @ParameterObject Pageable pageable
    ) {
        if (user.getRole() == Role.ADMIN) {
            boolean hasEmail = StringUtils.hasText(email);
            boolean hasStatus = status != null;

            if (hasEmail && hasStatus) {
                return ResponseEntity.ok(service.searchOrdersByUserEmailAndStatus(email, status, pageable));
            } else if (hasEmail) {
                return ResponseEntity.ok(service.getOrdersByUserEmail(email, pageable));
            } else if (hasStatus) {
                return ResponseEntity.ok(service.searchOrdersByStatus(status, pageable));
            }
            return ResponseEntity.ok(service.getAllOrders(pageable));
        } else {
            if (status != null) {
                return ResponseEntity.ok(service.searchOrdersByUserAndStatus(user.getId(), status, pageable));
            }
            return ResponseEntity.ok(service.getOrdersByUserId(user.getId(), pageable));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOrderById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request
    ) {
        return ResponseEntity.ok(service.updateOrderStatus(id, request.getStatus()));
    }
}
