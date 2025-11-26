package org.upnext.authservice.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.upnext.authservice.exceptions.UserNotFound;
import org.upnext.authservice.mappers.UserMapper;
import org.upnext.authservice.models.User;
import org.upnext.authservice.repositories.UserRepository;
import org.upnext.authservice.services.Impl.UserServiceImpl;
import org.upnext.sharedlibrary.Errors.Result;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;



    @Test
    void givenUser_whenSave_thenReturnSavedUser() {
        User user = new User();
        user.setName("test");
        user.setPassword("test");
        user.setEmail("test");
        user.setPhoneNumber("test");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.save(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser).isEqualTo(user);
    }

    @Test
    void givenEmailExists_whenExistsByEmail_thenReturnsTrue(){
        String email = "test@gmail.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        userService.existsByEmail(email);

        Boolean emailExists = userService.existsByEmail(email);

        assertThat(emailExists).isTrue();
    }

    @Test
    void givenEmailDoesNotExist_whenExistsByEmail_thenReturnsFalse(){
        String email =  "test@gmail.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        userService.existsByEmail(email);

        Boolean emailExists = userService.existsByEmail(email);
        assertThat(emailExists).isFalse();
    }

    @Test
    void givenEmailExists_whenLoadUserByEmail_thenReturnsUser() {
        String email = "test@gmail.com";
        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        User savedUser = userService.loadUserByEmail(email);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo(email);
    }

    @Test
    void givenEmailDoesNotExist_whenLoadUserByEmail_thenThrowsUserNotFound() {
        String email = "testNotFound@gmail.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserNotFound.class, () -> userService.loadUserByEmail(email));
    }

    @Test
    void givenCorrectOldPassword_whenUpdatePassword_thenSuccess(){
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String oldEncodedPassword = "oldEncodedPassword";
        String newEncodedPassword = "newEncodedPassword";

        Long userId = 1L;
        User user = new  User();
        user.setId(userId);
        user.setPassword(oldEncodedPassword);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(newEncodedPassword);

        Result<Void> result = userService.updatePassword(userId, oldPassword, newPassword);

        assertThat(result.isSuccess()).isTrue();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo(newEncodedPassword);
    }

    @Test
    void givenIncorrectOldPassword_whenUpdatePassword_thenReturnsFalse() {
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String oldEncodedPassword = "oldEncodedPassword";
        String newEncodedPassword = "newEncodedPassword";
        Long userId = 1L;
        User user = new  User();
        user.setId(userId);
        user.setPassword(oldEncodedPassword);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(false);

        Result<Void> result = userService.updatePassword(userId, oldPassword, newPassword);
        assertThat(result.isSuccess()).isFalse();

    }
}
