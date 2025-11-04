package org.upnext.authservice.services.Impl;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.upnext.authservice.dtos.request.UpdateUserRequest;
import org.upnext.authservice.enums.Roles;
import org.upnext.authservice.exceptions.UserNotFound;
import org.upnext.authservice.jwt.JwtUtils;
import org.upnext.authservice.mappers.UserMapper;
import org.upnext.authservice.models.User;
import org.upnext.authservice.repositories.UserRepository;
import org.upnext.authservice.services.UserService;
import org.upnext.sharedlibrary.Dtos.UserDto;

import org.upnext.sharedlibrary.Errors.*;
import org.upnext.sharedlibrary.Errors.Error;

import java.util.List;

import static org.upnext.authservice.utils.CopyFieldsToClass.copyNonNullAndNonBlankFields;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User loadUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFound("No user with such email"));
    }

    @Override
    @Transactional
    public void updatePassword(User user, String password) {

        user.setPassword(password);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public Result<Void> updatePassword(Long id, String oldPassword, String newPassword) {
        User user = loadUserObjectById(id);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.failure(new Error("Password.Incorrect", "Old password is incorrect", 400));
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return Result.success();
    }

    @Override
    public User loadUserObjectById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFound("No user with such id"));
    }

    @Override
    public UserDto loadUserDtoById(Long id) {
        return userMapper.toUserDto(loadUserObjectById(id));
    }

    @Override
    public List<UserDto> loadAllConfirmedUsers() {
        List<User> users = userRepository.findAllByIsConfirmed(true);
        return users.stream()
                .map(userMapper::toUserDto)
                .toList();
    }

    @Override
    public List<UserDto> loadAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toUserDto)
                .toList();
    }
    void updateResponseJwts(HttpServletResponse response, String token){
        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setMaxAge(24 * 60 * 60);
        response.addCookie(jwtCookie);

    }
    @Override
    public Result<Void> makeAdmin(Long id, HttpServletResponse response) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFound("No user with such id"));
        user.getRoles().add(Roles.ADMIN);
        userRepository.save(user);

        String token = jwtUtils.generateToken(user);
        updateResponseJwts(response, token);
        return Result.success();
    }

    @Override
    public Result<Void> updateUser(Long userId, UpdateUserRequest updateUserRequest, HttpServletResponse response, Boolean isAdmin) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFound("No user with such id"));
        copyNonNullAndNonBlankFields(updateUserRequest, user);
        userRepository.save(user);
        // if this user is admin so we do not want to update the token
        if(!isAdmin) {
            String token = jwtUtils.generateToken(user);
            updateResponseJwts(response, token);
        }
        return Result.success();
    }


}
