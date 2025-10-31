package org.upnext.authservice.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.upnext.authservice.validation.FieldsMatch;
import org.upnext.authservice.validation.PasswordValidator;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldsMatch(first = "password", second = "passwordConfirm", message = "Passwords must match")
public class PasswordChangeRequest {

    @NotBlank
    @PasswordValidator
    private String oldPassword;


    @NotBlank
    @PasswordValidator
    private String newPassword;

    @NotBlank
    @PasswordValidator
    private String newPasswordConfirm;

}
