package org.upnext.authservice.Dtos.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// when user enters his male and forget password
public class PasswordResetWithMailRequest {
    @Email(message = "Invalid email")
    @NotBlank(message = "Email failed mustn't be empty!")
    private String email;

}
