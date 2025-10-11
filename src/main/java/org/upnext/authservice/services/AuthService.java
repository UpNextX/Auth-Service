package org.upnext.authservice.services;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.upnext.authservice.dtos.request.EmailRequest;
import org.upnext.authservice.dtos.request.LoginRequest;
import org.upnext.authservice.dtos.request.PasswordResetRequest;
import org.upnext.authservice.dtos.request.RegisterRequest;
import org.upnext.authservice.dtos.response.LoginResponse;

public interface AuthService {
    LoginResponse login(@Valid @RequestBody LoginRequest loginRequest);

    void register(@Valid @RequestBody RegisterRequest registerRequest);

    void confirmAccount(String token);

    void requireConfirmation(EmailRequest emailRequest);

    void sendPasswordResetMail(EmailRequest emailRequest);

    void passwordReset(@Valid @RequestBody PasswordResetRequest passwordResetRequest);

}
