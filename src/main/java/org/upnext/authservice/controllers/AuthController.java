package org.upnext.authservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.upnext.authservice.Dtos.Request.LoginRequest;
import org.upnext.authservice.Dtos.Request.PasswordResetRequest;
import org.upnext.authservice.Dtos.Request.RegisterRequest;
import org.upnext.authservice.models.PasswordReset;
import org.upnext.authservice.models.User;
import org.upnext.authservice.services.AuthService;

import java.net.URI;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest){

    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest){

    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody String refreshToken){

    }

    @GetMapping("/reset")
    public ResponseEntity<?> reset(){
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forget-password")
    public ResponseEntity<?> forgetPassword(){

    }

    @PostMapping("/reset-password")
    public ResponseEntity<URI> resetPassword(@RequestBody PasswordResetRequest passwordResetRequest){

    }
}
