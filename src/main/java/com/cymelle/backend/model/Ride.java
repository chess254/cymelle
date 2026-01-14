package com.cymelle.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rides")
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"id", "firstName", "lastName", "role"})
    private User customer;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    @JsonIgnoreProperties({"id", "firstName", "lastName", "role"})
    private User driver;

    @Column(nullable = false)
    private String pickupLocation;

    @Column(nullable = false)
    private String dropoffLocation;

    private BigDecimal fare;

    @Enumerated(EnumType.STRING)
    private RideStatus status;

    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        requestedAt = LocalDateTime.now();
        if (status == null) {
            status = RideStatus.REQUESTED;
        }
    }
}
