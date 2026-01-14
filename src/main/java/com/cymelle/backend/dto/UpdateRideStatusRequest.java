package com.cymelle.backend.dto;

import com.cymelle.backend.model.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRideStatusRequest {
    private RideStatus status;
}
