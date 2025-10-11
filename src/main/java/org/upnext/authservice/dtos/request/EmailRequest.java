package org.upnext.authservice.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// when user enters his male and forget password
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {
    @Email(message = "Invalid email")
    @NotBlank(message = "Email failed mustn't be empty!")
    private String email;

}
