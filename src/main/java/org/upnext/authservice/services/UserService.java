package org.upnext.authservice.services;

import jakarta.servlet.http.HttpServletResponse;
import org.upnext.authservice.dtos.request.UpdateUserRequest;
import org.upnext.authservice.models.User;
import org.upnext.sharedlibrary.Dtos.UserDto;
import org.upnext.sharedlibrary.Errors.*;


import java.util.List;

public interface UserService {

    User save(User user);

    Boolean existsByEmail(String email);

    User loadUserByEmail(String email);

    void updatePassword(User user, String password);

    Result<Void> updatePassword(Long id, String oldPassword, String newPassword);

    User loadUserObjectById(Long id);

    UserDto loadUserDtoById(Long id);
    List<UserDto> loadAllConfirmedUsers();

    List<UserDto> loadAllUsers();

    Result<Void> makeAdmin(Long id, HttpServletResponse response);

    Result<Void> updateUser(Long userId, UpdateUserRequest updateUserRequest, HttpServletResponse response, Boolean isAdmin);
}
