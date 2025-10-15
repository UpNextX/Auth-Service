package org.upnext.authservice.controllers;


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
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDto user) {
        return ResponseEntity.ok(userService.loadUserDtoById(user.getId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok().body(userService.loadAllUsers());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PutMapping("/me/password")
    public ResponseEntity<?> updatePassword(@AuthenticationPrincipal UserDto user, @RequestBody PasswordChangeRequest passwordChangeRequest) {
        Result<Void> result = userService.updatePassword(user.getId(), passwordChangeRequest.getOldPassword(), passwordChangeRequest.getNewPassword());
        if(result.isSuccess()) {
            return ResponseEntity.noContent().build();

        }
        return ResponseEntity.status(result.getError().getStatusCode()).body(result.getError().getMessage());
    }


}