package org.upnext.authservice.dtos.request;


import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.upnext.authservice.validation.FieldsMatch;
import org.upnext.authservice.validation.PasswordValidator;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldsMatch(first = "email", second = "emailConfirm", message = "Emails must match")
@FieldsMatch(first = "password", second = "passwordConfirm", message = "Passwords must match")
public class RegisterRequest {
    @NotBlank(message = "Name must not be blank")
    @Size(min = 2, message = "To short name")
    private String name;

    @NotBlank(message = "Phone number must not be blank")
    @Pattern(regexp = "(^01[0-2][0-9]{8})", message = "Mobile number must be 11 digits")
    private String phoneNumber;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Please provide a valid email")
    private String email;

    @NotBlank(message = "Email confirmation must not be blank")
    @Email(message = "Please provide a valid email confirmation")
    private String emailConfirm;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, max = 32, message = "Password must be between 8 and 32 characters!")
    @PasswordValidator
    String password;

    @NotBlank(message = "Password confirmation must not be blank")
    @Size(min = 8, max = 32, message = "Password confirmation must be between 8 and 32 characters!")
    String passwordConfirm;


    @NotBlank(message = "Address must not be blank")
    private String address;
}
