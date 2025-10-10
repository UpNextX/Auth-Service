package org.upnext.authservice.Dtos.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// this comes from front
@Data
public class PasswordResetRequest {
    @NotBlank
    private String token;
    @NotBlank
    private String newPassword;
}