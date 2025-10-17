package org.upnext.authservice.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.upnext.authservice.dtos.request.EmailRequest;
import org.upnext.authservice.dtos.request.LoginRequest;
import org.upnext.authservice.dtos.request.PasswordResetRequest;
import org.upnext.authservice.dtos.request.RegisterRequest;
import org.upnext.authservice.dtos.response.LoginResponse;
import org.upnext.authservice.models.User;
import org.upnext.sharedlibrary.Dtos.UserDto;
import org.upnext.sharedlibrary.Errors.Result;

public interface AuthService {
    Result<UserDto> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response);

    Result<UserDto> register(@Valid @RequestBody RegisterRequest registerRequest, HttpServletResponse response);

    void confirmAccount(String token);

    Result<String> requireConfirmation(EmailRequest emailRequest);

    void sendPasswordResetMail(EmailRequest emailRequest);

    void passwordReset(String token, String password);

    void logout(HttpServletResponse response);
}
