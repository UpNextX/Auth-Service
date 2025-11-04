package org.upnext.authservice.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.upnext.authservice.dtos.request.PasswordChangeRequest;
import org.upnext.authservice.dtos.request.UpdateUserRequest;
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

    @PutMapping("/me/admin")
    public ResponseEntity<?> updateUserRole(@AuthenticationPrincipal UserDto user, HttpServletResponse response) {
        userService.makeAdmin(user.getId(), response);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me")
    public ResponseEntity<?> updateUserData(@AuthenticationPrincipal UserDto user,@RequestBody UpdateUserRequest updateUserRequest, HttpServletResponse response) {
        userService.updateUser(user.getId(), updateUserRequest, response, false);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Update user data with user id for admins only",
            responses = {
                    @ApiResponse(responseCode = "204",description = "Updated user data successfully"),
                    @ApiResponse(responseCode = "401", description = "UnAuthorized")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserDataAdmin(@PathVariable Long id,@RequestBody UpdateUserRequest updateUserRequest, HttpServletResponse response) {
        userService.updateUser(id, updateUserRequest, response, true);
        return ResponseEntity.noContent().build();
    }


}