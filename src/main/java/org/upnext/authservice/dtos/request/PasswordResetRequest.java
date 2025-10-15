package org.upnext.authservice.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.upnext.authservice.validation.PasswordValidator;

// this comes from front with (token -> sent from back & new password)
@Data
public class PasswordResetRequest {
    @NotBlank
    @PasswordValidator
    private String newPassword;
}