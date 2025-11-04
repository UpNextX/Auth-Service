package org.upnext.authservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Authentication",
        description = "Endpoints for user registration, login, logout, and password management."
)

public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "User login",
            description = "Authenticates a user and returns a JWT token with user info.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully authenticated",
                            content = @Content(schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials or account not confirmed")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response, Authentication authentication) {
        System.out.println("Logging in controller");
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

    @Operation(
            summary = "Logout current user",
            description = "Logs out the currently authenticated user by clearing authentication cookies.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully logged out"),
                    @ApiResponse(responseCode = "400", description = "No user is logged in")
            }
    )
    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response, Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        authService.logout(response);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(
            summary = "Register new user",
            description = "Registers a new user and sends a confirmation email.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User registered successfully"),
                    @ApiResponse(responseCode = "400", description = "Already authenticated"),
                    @ApiResponse(responseCode = "409", description = "Email already used")
            }
    )
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest, Authentication authentication, HttpServletResponse response) {
        if(authentication != null && authentication.isAuthenticated()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Already Signed in!");
        }
        Result<Void>result = authService.register(registerRequest, response);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result.getValue());
    }

    @Operation(
            summary = "Confirm account email",
            description = "Verifies a user's email address using a confirmation token.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Email confirmed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid or missing token"),
                    @ApiResponse(responseCode = "409", description = "Token has already been used"),
                    @ApiResponse(responseCode = "410", description = "Token has expired")


            }
    )
    @GetMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestParam("token") String token) {
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        authService.confirmAccount(token);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Request confirmation email again",
            description = "Resends a confirmation email if the user did not receive it.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Email sent successfully")
            }
    )
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


    @Operation(
            summary = "Forgot password",
            description = "Sends a password reset link to the user's email.",
            responses = {
                    @ApiResponse(responseCode = "202", description = "Password reset email sent"),
                    @ApiResponse(responseCode = "400", description = "Already authenticated"),
            }
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgetPassword(@RequestBody @Valid EmailRequest passwordResetWithMail, Authentication authentication) {
        if(authentication != null && authentication.isAuthenticated()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Already Signed in!");
        }

        authService.sendPasswordResetMail(passwordResetWithMail);
        return ResponseEntity.accepted().build();
    }

    @Operation(
            summary = "Reset password",
            description = "Resets user's password using a valid reset token.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Password reset successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or already authenticated or missing token"),
                    @ApiResponse(responseCode = "409", description = "Token has already been used"),
                    @ApiResponse(responseCode = "410", description = "Token has expired")

            }
    )
    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest passwordResetRequest, @RequestParam("token") String token, Authentication authentication) {
        if(authentication != null && authentication.isAuthenticated()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Already Signed in!");
        }

        authService.passwordReset(token, passwordResetRequest.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}
