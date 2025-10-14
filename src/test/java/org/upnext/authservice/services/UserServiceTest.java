package org.upnext.authservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.upnext.authservice.exceptions.UserNotFound;
import org.upnext.authservice.models.User;
import org.upnext.authservice.repositories.UserRepository;
import org.upnext.authservice.services.Impl.UserServiceImpl;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Test
    public void givenNonExistingEmail_whenExistsByEmail_thenReturnFalse(){
        String email = "email";
        assertThat(userService.existsByEmail(email)).isFalse();
    }

    @Test
    public void givenNonExistsEmail_whenFindByEmail_thenThrowUserNotFoundException(){
        String email = "email";
        assertThrows(UserNotFound.class, () -> userService.loadUserByEmail(email));

    }

    @Test
    public void givenValidUser_whenSave_thenTrue(){
        User user = new User();
        user.setEmail("eslam@gmail.com");
        user.setPassword("password");
        user.setIsConfirmed(false);
        user.setName("name");
        user.setPhoneNumber("01029842716");
        user.setAddress("Cario");
        when(userRepository.save(any(User.class))).thenReturn(user);
        assertThat(userService.save(user)).isNotNull();
    }
}
