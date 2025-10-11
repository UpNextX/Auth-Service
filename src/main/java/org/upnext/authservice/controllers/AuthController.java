package org.upnext.authservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.upnext.authservice.dtos.request.LoginRequest;
import org.upnext.authservice.dtos.request.PasswordResetRequest;
import org.upnext.authservice.dtos.request.EmailRequest;
import org.upnext.authservice.dtos.request.RegisterRequest;
import org.upnext.authservice.dtos.response.LoginResponse;
import org.upnext.authservice.services.AuthService;

import java.net.URI;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest){
        LoginResponse result = authService.login(loginRequest);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest){
        authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestParam("token") String token){
        if(token == null || token.trim().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        authService.confirmAccount(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/require-confirm")
    public ResponseEntity<?> requireConfirm(@RequestBody EmailRequest emailRequest){
        authService.requireConfirmation(emailRequest);

        return ResponseEntity.noContent().build();
    }

    // TODO
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody String refreshToken){
        return null;
    }



    @PostMapping("/forget-password")
    public ResponseEntity<?> forgetPassword(@RequestBody @Valid EmailRequest passwordResetWithMail){
        authService.sendPasswordResetMail(passwordResetWithMail);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest passwordResetRequest){
        authService.passwordReset(passwordResetRequest);
        return ResponseEntity.noContent().build();
    }
}
