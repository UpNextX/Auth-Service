package org.upnext.authservice.services;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import org.upnext.authservice.dtos.request.EmailRequest;
import org.upnext.authservice.dtos.request.LoginRequest;
import org.upnext.authservice.dtos.request.RegisterRequest;
import org.upnext.authservice.enums.TokenType;
import org.upnext.authservice.exceptions.EmailAlreadyUsed;
import org.upnext.authservice.exceptions.TokenNotFound;
import org.upnext.authservice.jwt.JwtUtils;
import org.upnext.authservice.mappers.UserMapper;
import org.upnext.authservice.models.Token;
import org.upnext.authservice.models.User;
import org.upnext.authservice.repositories.TokensRepository;
import org.upnext.authservice.repositories.UserRepository;
import org.upnext.authservice.services.Impl.AuthServiceImpl;
import org.upnext.sharedlibrary.Dtos.UserDto;
import org.upnext.sharedlibrary.Errors.Result;
import jakarta.servlet.http.Cookie;
import org.upnext.sharedlibrary.Events.MailEvent;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private  UserService userService;
    @Mock
    private  UserMapper userMapper;
    @Mock
    private  PasswordEncoder passwordEncoder;
    @Mock
    private  TokensRepository tokensRepository;
    @Mock
    private  JwtUtils jwtUtils;
    @Mock
    private  RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AuthServiceImpl authService;


    @Test
    void givenExistingUser_whenRegister_thenThrowsEmailAlreadyUsed(){
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("existing@gmail.com");

        when(userService.existsByEmail(registerRequest.getEmail())).thenReturn(true);
        HttpServletResponse response = mock(HttpServletResponse.class);

        assertThatThrownBy(()->authService.register(registerRequest, response))
                .isInstanceOf(EmailAlreadyUsed.class)
                .hasMessage("Email already used!");

        verify(userService, never()).save(any(User.class));
        verify(jwtUtils, never()).generateToken(any(User.class));

    }
    @Test
    public void givenInvalidCredentials_whenLogin_thenError() {
        RegisterRequest registerRequest = new RegisterRequest();
        String password = "password";
        String encodedPassword = "encodedPassword";
        registerRequest.setEmail("email@gmai.com");
        String generatedToken = "dummy.jwt.token";
        registerRequest.setPassword(password);

        User user = new User();
        user.setId(1L);
        user.setEmail(registerRequest.getEmail());
        user.setPassword(encodedPassword);

        UserDto userDto = new UserDto();
        userDto.setEmail(registerRequest.getEmail());

        Token token = Token.builder()
                .token("confirmation-token")
                .user(user)
                .type(TokenType.ACCOUNT_CONFIRM)
                .isUsed(false)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();

        when(userService.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userMapper.toUserFromRegisterRequest(registerRequest)).thenReturn(user);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userService.save(any(User.class))).thenReturn(user);
        when(tokensRepository.save(any(Token.class))).thenReturn(token);
        when(userService.loadUserByEmail(registerRequest.getEmail())).thenReturn(user);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtUtils.generateToken(user)).thenReturn(generatedToken);
        when(userMapper.toUserDto(user)).thenReturn(userDto);
        HttpServletResponse response = mock(HttpServletResponse.class);

        Result<Void> result = authService.register(registerRequest, response);
        assertThat(result.isSuccess()).isTrue();
        ArgumentCaptor<MailEvent> mailEventCaptor = ArgumentCaptor.forClass(MailEvent.class);

        verify(userService).save(any(User.class));
        verify(tokensRepository).save(any(Token.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), mailEventCaptor.capture());
        assertThat(mailEventCaptor.getValue().getEmail()).isEqualTo(registerRequest.getEmail());
        verify(response).addCookie(any(Cookie.class));

    }

    @Test
    void givenInValidCredentials_whenLogIn_thenFailed() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("email@gmail.com");
        loginRequest.setPassword("password");
        String encodedPassword = "encodedPassword";
        User user = new User();
        user.setId(1L);
        user.setEmail(loginRequest.getEmail());
        user.setPassword(encodedPassword);
        when(userService.loadUserByEmail(loginRequest.getEmail())).thenReturn(user);
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);
        HttpServletResponse response = mock(HttpServletResponse.class);

        Result<UserDto> result = authService.login(loginRequest, response);
        assertThat(result.isSuccess()).isFalse();
        verify(jwtUtils, never()).generateToken(any(User.class));
        verify(response, never()).addCookie(any(Cookie.class));
    }

    @Test
    void givenCredentials_whenLogIn_thenReturnsUserDto() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("email@gmail.com");
        loginRequest.setPassword("password");
        String token = "login-token";
        String encodedPassword = "encodedPassword";
        User user = new User();
        user.setId(1L);
        user.setEmail(loginRequest.getEmail());
        user.setPassword(encodedPassword);
        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        userDto.setId(user.getId());
        userDto.setId(user.getId());
        when(userService.loadUserByEmail(loginRequest.getEmail())).thenReturn(user);
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtUtils.generateToken(user)).thenReturn(token);
        when(userMapper.toUserDto(user)).thenReturn(userDto);
        HttpServletResponse response = mock(HttpServletResponse.class);

        Result<UserDto> result = authService.login(loginRequest, response);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getEmail()).isEqualTo(loginRequest.getEmail());
        verify(response, atMostOnce()).addCookie(any(Cookie.class));

    }

    @Test
    void givenValidToken_whenConfirmAccount_thenAccountConfirmed() {
        String tokenStr = "valid-token";

        User user = new User();
        user.setIsConfirmed(false);

        Token token = Token.builder()
                .token(tokenStr)
                .user(user)
                .type(TokenType.ACCOUNT_CONFIRM)
                .isUsed(false)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();

        when(tokensRepository.findByTokenAndType(tokenStr, TokenType.ACCOUNT_CONFIRM))
                .thenReturn(Optional.of(token));
        when(userService.save(user)).thenReturn(user);

        authService.confirmAccount(tokenStr);

        assertThat(user.getIsConfirmed()).isTrue();
        assertThat(token.getIsUsed()).isTrue();
        verify(userService).save(user);
        verify(tokensRepository).save(token);
    }

    @Test
    void givenInvalidToken_whenConfirmAccount_thenThrowsTokenNotFound() {
        String tokenStr = "invalid-token";

        when(tokensRepository.findByTokenAndType(tokenStr, TokenType.ACCOUNT_CONFIRM))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.confirmAccount(tokenStr))
                .isInstanceOf(TokenNotFound.class)
                .hasMessage("Invalid or unknown token!");

        verify(userService, never()).save(any(User.class));
    }
    @Test
    void givenExpiredToken_whenConfirmAccount_thenThrowsResponseStatusException() {
        String tokenStr = "expired-token";

        Token token = Token.builder()
                .token(tokenStr)
                .user(new User())
                .type(TokenType.ACCOUNT_CONFIRM)
                .isUsed(false)
                .expiryDate(LocalDateTime.now().minusDays(1))
                .build();

        when(tokensRepository.findByTokenAndType(tokenStr, TokenType.ACCOUNT_CONFIRM))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.confirmAccount(tokenStr))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Token has expired");

        verify(userService, never()).save(any(User.class));
    }

    @Test
    void givenUsedToken_whenConfirmAccount_thenThrowsResponseStatusException() {
        String tokenStr = "used-token";

        Token token = Token.builder()
                .token(tokenStr)
                .user(new User())
                .type(TokenType.ACCOUNT_CONFIRM)
                .isUsed(true)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();

        when(tokensRepository.findByTokenAndType(tokenStr, TokenType.ACCOUNT_CONFIRM))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.confirmAccount(tokenStr))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Token has already been used");

        verify(userService, never()).save(any(User.class));
    }
    @Test
    void givenUnconfirmedAccount_whenRequireConfirmation_thenSendsMail() {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmail("user@gmail.com");

        User user = new User();
        user.setEmail(emailRequest.getEmail());
        user.setIsConfirmed(false);

        Token token = Token.builder()
                .token("confirmation-token")
                .user(user)
                .type(TokenType.ACCOUNT_CONFIRM)
                .isUsed(false)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();

        when(userService.loadUserByEmail(emailRequest.getEmail())).thenReturn(user);
        when(tokensRepository.save(any(Token.class))).thenReturn(token);

        Result<String> result = authService.requireConfirmation(emailRequest);
        ArgumentCaptor<MailEvent> mailEventCaptor = ArgumentCaptor.forClass(MailEvent.class);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo("Confirmation mail send.");
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), mailEventCaptor.capture());
        assertThat(mailEventCaptor.getValue().getEmail()).isEqualTo(emailRequest.getEmail());
    }

    @Test
    void givenConfirmedAccount_whenRequireConfirmation_thenReturnsError() {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmail("user@gmail.com");

        User user = new User();
        user.setEmail(emailRequest.getEmail());
        user.setIsConfirmed(true);

        when(userService.loadUserByEmail(emailRequest.getEmail())).thenReturn(user);

        Result<String> result = authService.requireConfirmation(emailRequest);
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    void givenValidEmail_whenSendPasswordResetMail_thenSendsEmail() {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmail("user@gmail.com");

        User user = new User();
        user.setEmail(emailRequest.getEmail());
        user.setName("Test User");

        Token token = Token.builder()
                .token("reset-token")
                .user(user)
                .type(TokenType.PASSWORD_RESET)
                .isUsed(false)
                .expiryDate(LocalDateTime.now().plusHours(4))
                .build();

        when(userService.loadUserByEmail(emailRequest.getEmail())).thenReturn(user);
        when(tokensRepository.save(any(Token.class))).thenReturn(token);

        authService.sendPasswordResetMail(emailRequest);
        ArgumentCaptor<MailEvent> mailEventCaptor = ArgumentCaptor.forClass(MailEvent.class);

        verify(tokensRepository).save(any(Token.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), mailEventCaptor.capture());
        assertThat(mailEventCaptor.getValue().getEmail()).isEqualTo(emailRequest.getEmail());
    }

    @Test
    void givenValidToken_whenPasswordReset_thenUpdatesPassword() {
        String tokenStr = "reset-token";
        String newPassword = "newPassword123";
        String encodedPassword = "encodedNewPassword";

        User user = new User();
        user.setEmail("user@gmail.com");

        Token token = Token.builder()
                .token(tokenStr)
                .user(user)
                .type(TokenType.PASSWORD_RESET)
                .isUsed(false)
                .expiryDate(LocalDateTime.now().plusHours(4))
                .build();

        when(tokensRepository.findByTokenAndType(tokenStr, TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        authService.passwordReset(tokenStr, newPassword);

        assertThat(token.getIsUsed()).isTrue();
        verify(userService).updatePassword(user, encodedPassword);
    }

    @Test
    void givenNullToken_whenPasswordReset_thenThrowsTokenNotFound() {
        String newPassword = "newPassword123";

        assertThatThrownBy(() -> authService.passwordReset(null, newPassword))
                .isInstanceOf(TokenNotFound.class)
                .hasMessage("Invalid or unknown token!");

        verify(userService, never()).updatePassword(any(User.class), anyString());
    }

    @Test
    void givenInvalidToken_whenPasswordReset_thenThrowsTokenNotFound() {
        String tokenStr = "invalid-token";
        String newPassword = "newPassword123";

        when(tokensRepository.findByTokenAndType(tokenStr, TokenType.PASSWORD_RESET))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.passwordReset(tokenStr, newPassword))
                .isInstanceOf(TokenNotFound.class)
                .hasMessage("Invalid or unknown token!");

        verify(userService, never()).updatePassword(any(User.class), anyString());
    }

    @Test
    void whenLogout_thenClearsCookie() {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        authService.logout(mockResponse);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(mockResponse).addCookie(cookieCaptor.capture());

        Cookie cookie = cookieCaptor.getValue();
        assertThat(cookie.getName()).isEqualTo("jwt");
        assertThat(cookie.getValue()).isNull();
        assertThat(cookie.getMaxAge()).isEqualTo(0);
    }

}
