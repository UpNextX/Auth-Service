package org.upnext.authservice.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 32, message = "Password must be between 8 and 32 characters!")
    String password;
}
