package com.cymelle.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RideRequest {
    @NotBlank(message = "Pickup location is required")
    @Schema(description = "Address or point where the ride starts", example = "City Center Mall")
    private String pickupLocation;

    @NotBlank(message = "Dropoff location is required")
    @Schema(description = "Address or point where the ride ends", example = "Westside Apartments")
    private String dropoffLocation;
}
