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

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService service;

    @PostMapping
    public ResponseEntity<Ride> requestRide(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid RideRequest request
    ) {
        return ResponseEntity.ok(service.requestRide(user, request));
    }

    @GetMapping
    public ResponseEntity<Page<Ride>> getRides(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) RideStatus status,
            Pageable pageable
    ) {
        if (status != null) {
            return ResponseEntity.ok(service.searchRidesByStatus(status, pageable));
        }
        return ResponseEntity.ok(service.getRidesByCustomer(user, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ride> getRideById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getRideById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Ride> updateRideStatus(
            @PathVariable Long id,
            @RequestBody UpdateRideStatusRequest request,
            @AuthenticationPrincipal User driver // Assuming the one updating is the driver or admin
    ) {
        return ResponseEntity.ok(service.updateRideStatus(id, request.getStatus(), driver));
    }
}
