package org.upnext.authservice.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.upnext.authservice.jwt.JwtUtils;
import org.upnext.authservice.dtos.request.LoginRequest;
import org.upnext.authservice.dtos.request.PasswordResetRequest;
import org.upnext.authservice.dtos.request.EmailRequest;
import org.upnext.authservice.dtos.request.RegisterRequest;
import org.upnext.authservice.models.User;
import org.upnext.authservice.services.AuthService;
import org.upnext.sharedlibrary.Dtos.UserDto;
import org.upnext.sharedlibrary.Errors.Result;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response, Authentication authentication) {
        if(authentication != null && authentication.isAuthenticated()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Already Signed in!");
        }
        Result<UserDto> result = authService.login(loginRequest, response);
        if (result.getIsFailure()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(result.getError().getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(result.getValue());
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response, Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        authService.logout(response);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest, Authentication authentication) {
        if(authentication != null && authentication.isAuthenticated()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Already Signed in!");
        }
        authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestParam("token") String token) {
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        authService.confirmAccount(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/require-confirm")
    public ResponseEntity<?> requireConfirm(@RequestBody EmailRequest emailRequest, Authentication authentication) {

        Result<String> result = authService.requireConfirmation(emailRequest);
        if (result.getIsFailure()) {
            return ResponseEntity.status(result.getError()
                            .getStatusCode())
                    .body(result.getError().getMessage());
        }
        return ResponseEntity.noContent().build();
    }

    // TODO
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody String refreshToken) {
        return null;
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgetPassword(@RequestBody @Valid EmailRequest passwordResetWithMail, Authentication authentication) {
        if(authentication != null && authentication.isAuthenticated()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Already Signed in!");
        }

        authService.sendPasswordResetMail(passwordResetWithMail);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest passwordResetRequest, @RequestParam("token") String token, Authentication authentication) {
        System.out.println("RESET-PASSWORD");
        if(authentication != null && authentication.isAuthenticated()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Already Signed in!");
        }

        authService.passwordReset(token, passwordResetRequest.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}
