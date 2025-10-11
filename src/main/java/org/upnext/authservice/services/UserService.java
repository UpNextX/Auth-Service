package org.upnext.authservice.services;

import org.upnext.authservice.models.User;

public interface UserService {

    User save(User user);

    Boolean existsByEmail(String email);

    User findByEmail(String email);

    void updatePassword(User user, String password);

}
