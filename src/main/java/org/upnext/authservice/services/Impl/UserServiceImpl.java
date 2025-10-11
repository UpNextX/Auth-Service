package org.upnext.authservice.services.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.upnext.authservice.exceptions.UserNotFound;
import org.upnext.authservice.models.User;
import org.upnext.authservice.repositories.UserRepository;
import org.upnext.authservice.services.UserService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(()-> new UserNotFound("No user with such email"));
    }

    @Override
    public void updatePassword(User user, String password) {

        user.setPassword(password);
        userRepository.save(user);
    }


}
