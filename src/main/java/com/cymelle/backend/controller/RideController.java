package com.cymelle.backend.controller;

import com.cymelle.backend.dto.RideRequest;
import com.cymelle.backend.model.Ride;
import com.cymelle.backend.model.RideStatus;
import com.cymelle.backend.model.User;
import com.cymelle.backend.service.RideService;
import com.cymelle.backend.dto.UpdateRideStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.cymelle.backend.model.Role;
import org.springframework.security.access.AccessDeniedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
@Tag(name = "Rides")
@SecurityRequirement(name = "bearerAuth")
public class RideController {

    private final RideService service;

    @PostMapping
    @Operation(summary = "Request a new ride", description = "Creates a ride request. Authenticated customers only.")
    public ResponseEntity<Ride> requestRide(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid RideRequest request
    ) {
        return ResponseEntity.ok(service.requestRide(user, request));
    }

    @GetMapping
    public ResponseEntity<Page<Ride>> getRides(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) RideStatus status,
            @ParameterObject Pageable pageable
    ) {
        if (user.getRole() == Role.ADMIN) {
            // Admin search
            boolean hasEmail = StringUtils.hasText(email);
            boolean hasStatus = status != null;

            if (hasEmail && hasStatus) {
                return ResponseEntity.ok(service.searchRidesByCustomerEmailAndStatus(email, status, pageable));
            } else if (hasEmail) {
                return ResponseEntity.ok(service.getRidesByCustomerEmail(email, pageable));
            } else if (hasStatus) {
                return ResponseEntity.ok(service.searchRidesByStatus(status, pageable));
            }
            return ResponseEntity.ok(service.getAllRides(pageable));
        } else if (user.getRole() == Role.DRIVER) {
            // Driver search 
            if (status != null) {
                return ResponseEntity.ok(service.searchRidesByStatus(status, pageable));
            }
            return ResponseEntity.ok(service.getAllRides(pageable));
        } else {
            // Customer search
            if (status != null) {
                return ResponseEntity.ok(service.searchRidesByCustomerAndStatus(user.getId(), status, pageable));
            }
            return ResponseEntity.ok(service.getRidesByCustomerId(user.getId(), pageable));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ride by ID", description = "Admins can get any ride. Customers and Drivers can only get rides they are involved in.")
    public ResponseEntity<Ride> getRideById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        Ride ride = service.getRideById(id);
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isCustomer = ride.getCustomer().getId().equals(user.getId());
        boolean isDriver = ride.getDriver() != null && ride.getDriver().getId().equals(user.getId());

        if (!isAdmin && !isCustomer && !isDriver) {
            throw new AccessDeniedException("You do not have permission to view this ride.");
        }
        return ResponseEntity.ok(ride);
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Update ride status (Accept/Complete)",
            description = "Main endpoint for drivers to accept rides or complete them. Admins can also update any ride.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Status updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Illegal state transition or self-assignment"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Requires DRIVER or ADMIN role")
            }
    )
    public ResponseEntity<Ride> updateRideStatus(
            @PathVariable Long id,
            @RequestBody UpdateRideStatusRequest request,
            @AuthenticationPrincipal User driver
    ) {
        return ResponseEntity.ok(service.updateRideStatus(id, request.getStatus(), driver));
    }
}
