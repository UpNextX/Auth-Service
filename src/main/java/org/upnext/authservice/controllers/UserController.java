package org.upnext.authservice.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.upnext.authservice.dtos.request.PasswordChangeRequest;
import org.upnext.authservice.models.User;
import org.upnext.authservice.services.UserService;
import org.upnext.sharedlibrary.Dtos.UserDto;
import org.upnext.sharedlibrary.Errors.Result;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(
        name = "User Management",
        description = "APIs for accessing and updating user profiles and passwords."
)
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Get current logged-in user info",
            description = "Returns details of the currently authenticated user."
    )
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDto user) {
        return ResponseEntity.ok(userService.loadUserDtoById(user.getId()));
    }

    @Operation(
            summary = "Get all users (admin only)",
            description = "Returns a list of all users in the system."
    )

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok().body(userService.loadAllUsers());
    }

    @Operation(
            summary = "Update user password",
            description = "Allows user to change their password after authentication."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PutMapping("/me/password")
    public ResponseEntity<?> updatePassword(@AuthenticationPrincipal UserDto user, @RequestBody PasswordChangeRequest passwordChangeRequest) {
        Result<Void> result = userService.updatePassword(user.getId(), passwordChangeRequest.getOldPassword(), passwordChangeRequest.getNewPassword());
        if(result.isSuccess()) {
            return ResponseEntity.noContent().build();

        }
        return ResponseEntity.status(result.getError().getStatusCode()).body(result.getError().getMessage());
    }

    @Operation(
            summary = "Make current user an admin (testing/demo endpoint)",
            description = "Temporarily grants admin privileges to the current user."
    )
    @PutMapping("/me")
    public ResponseEntity<?> updateUser(@AuthenticationPrincipal UserDto user) {
        userService.makeAdmin(user.getId());
        return ResponseEntity.noContent().build();
    }


}