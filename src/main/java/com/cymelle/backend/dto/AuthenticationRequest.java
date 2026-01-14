package com.cymelle.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {
    @Schema(description = "User's registered email address", example = "admin@cymelle.com")
    private String email;
    @Schema(description = "User's password", example = "password123")
    private String password;
}
