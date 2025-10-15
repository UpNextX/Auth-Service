package org.upnext.authservice.services;

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

    List<UserDto> loadAllUsers();
}
