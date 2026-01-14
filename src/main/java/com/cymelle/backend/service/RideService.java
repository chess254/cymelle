package com.cymelle.backend.service;

import com.cymelle.backend.dto.RideRequest;
import com.cymelle.backend.model.Ride;
import com.cymelle.backend.model.RideStatus;
import com.cymelle.backend.model.User;
import com.cymelle.backend.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RideService {
    private final RideRepository rideRepository;

    public Ride requestRide(User customer, RideRequest request) {
        Ride ride = Ride.builder()
                .customer(customer)
                .pickupLocation(request.getPickupLocation())
                .dropoffLocation(request.getDropoffLocation())
                .status(RideStatus.REQUESTED)
                .fare(BigDecimal.ZERO) // Fare calculation logic can be added here
                .build();
        return rideRepository.save(ride);
    }

    public Page<Ride> getRidesByCustomer(User customer, Pageable pageable) {
        return rideRepository.findByCustomerId(customer.getId(), pageable);
    }

    public Ride getRideById(Long id) {
        return rideRepository.findById(id).orElseThrow(() -> new RuntimeException("Ride not found"));
    }

    public Ride updateRideStatus(Long id, RideStatus status, User driver) {
        Ride ride = getRideById(id);
        if (status == RideStatus.ACCEPTED && ride.getStatus() == RideStatus.REQUESTED) {
            ride.setDriver(driver);
            ride.setFare(BigDecimal.valueOf(25.0)); // Dummy fare assignment on acceptance
        } else if (status == RideStatus.COMPLETED) {
            ride.setCompletedAt(LocalDateTime.now());
        }
        ride.setStatus(status);
        return rideRepository.save(ride);
    }

    public Page<Ride> searchRidesByStatus(RideStatus status, Pageable pageable) {
        return rideRepository.findByStatus(status, pageable);
    }
}
