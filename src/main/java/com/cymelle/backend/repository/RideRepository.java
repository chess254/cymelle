package com.cymelle.backend.repository;

import com.cymelle.backend.model.Ride;
import com.cymelle.backend.model.RideStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RideRepository extends JpaRepository<Ride, Long> {
    Page<Ride> findByCustomerId(Long customerId, Pageable pageable);
    Page<Ride> findByStatus(RideStatus status, Pageable pageable);
    Page<Ride> findByCustomerIdAndStatus(Long customerId, RideStatus status, Pageable pageable);
    Page<Ride> findByCustomerEmail(String email, Pageable pageable);
    Page<Ride> findByCustomerEmailAndStatus(String email, RideStatus status, Pageable pageable);
}
