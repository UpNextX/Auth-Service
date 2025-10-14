package org.upnext.authservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.upnext.authservice.dtos.request.RegisterRequest;
import org.upnext.authservice.enums.TokenType;
import org.upnext.authservice.models.Token;
import org.upnext.authservice.models.User;
import org.upnext.authservice.repositories.TokensRepository;
import org.upnext.authservice.repositories.UserRepository;
import org.upnext.authservice.services.Impl.AuthServiceImpl;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private TokensRepository tokensRepository;

    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private User getUser(RegisterRequest registerRequest) {
        return User.builder()
                .name(registerRequest.getName())
                .id(1L)
                .email(registerRequest.getEmail())
                .password(registerRequest.getPassword())
                .phoneNumber(registerRequest.getPhoneNumber())
                .address(registerRequest.getAddress())
                .role("USER")
                .isConfirmed(false)
                .build();
    }
    @Test
    public void givenValidData_whenRegister_thenStatusCreated() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Ahmed");
        registerRequest.setEmail("Eslam@yahoo.com");
        registerRequest.setEmailConfirm("Eslam@yahoo.com");
        registerRequest.setPassword("rsdfgjelaskt32@A");
        registerRequest.setPasswordConfirm("rsdfgjelaskt32@A");
        registerRequest.setAddress("Cairo");
        registerRequest.setPhoneNumber("01022222222");
        User user = getUser(registerRequest);
        when(userService.save(any(User.class))).thenReturn(user);
        when(tokensRepository.save(any(Token.class))).thenReturn(Token.builder()
                .user(user)
                .token("abc123")
                .isUsed(false)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .type(TokenType.ACCOUNT_CONFIRM)
                .build());
        authService.register(registerRequest);

    }
}
