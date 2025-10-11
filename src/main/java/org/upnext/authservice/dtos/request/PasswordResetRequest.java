package org.upnext.authservice.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// this comes from front with (token -> sent from back & new password)
@Data
public class PasswordResetRequest {
    @NotBlank
    private String token;
    @NotBlank
    private String newPassword;
}