package com.cymelle.backend.service;

import com.cymelle.backend.dto.RideRequest;
import com.cymelle.backend.exception.ResourceNotFoundException;
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
                .fare(BigDecimal.ZERO) 
                .build();
        return rideRepository.save(ride);
    }

    public Page<Ride> getRidesByCustomerId(Long customerId, Pageable pageable) {
        return rideRepository.findByCustomerId(customerId, pageable);
    }

    public Ride getRideById(Long id) {
        return rideRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Ride not found with id: " + id));
    }

    public Ride updateRideStatus(Long id, RideStatus status, User user) {
        Ride ride = getRideById(id);
        
        if (status == RideStatus.ACCEPTED) {
            if (ride.getStatus() != RideStatus.REQUESTED) {
                throw new IllegalStateException("Ride can only be accepted if it is in REQUESTED status.");
            }
            if (ride.getCustomer().getId().equals(user.getId())) {
                throw new IllegalStateException("Drivers cannot accept a ride they requested as a customer.");
            }
            ride.setDriver(user);
            ride.setFare(BigDecimal.valueOf(25.0)); // Dummy fare assignment
        } else if (status == RideStatus.COMPLETED) {
            if (ride.getStatus() != RideStatus.ACCEPTED) {
                throw new IllegalStateException("Ride can only be completed after it has been accepted.");
            }
            ride.setCompletedAt(LocalDateTime.now());
        } else if (status == RideStatus.CANCELLED) {
            if (ride.getStatus() == RideStatus.COMPLETED) {
                 throw new IllegalStateException("Cannot cancel a completed ride.");
            }
        }
        
        ride.setStatus(status);
        return rideRepository.save(ride);
    }

    public Page<Ride> searchRidesByStatus(RideStatus status, Pageable pageable) {
        return rideRepository.findByStatus(status, pageable);
    }

    public Page<Ride> getAllRides(Pageable pageable) {
        return rideRepository.findAll(pageable);
    }

    public Page<Ride> searchRidesByCustomerAndStatus(Long customerId, RideStatus status, Pageable pageable) {
        return rideRepository.findByCustomerIdAndStatus(customerId, status, pageable);
    }

    public Page<Ride> getRidesByCustomerEmail(String email, Pageable pageable) {
        return rideRepository.findByCustomerEmail(email, pageable);
    }

    public Page<Ride> searchRidesByCustomerEmailAndStatus(String email, RideStatus status, Pageable pageable) {
        return rideRepository.findByCustomerEmailAndStatus(email, status, pageable);
    }
}
