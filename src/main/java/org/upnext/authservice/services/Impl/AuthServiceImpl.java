package org.upnext.authservice.services.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.upnext.authservice.dtos.request.EmailRequest;
import org.upnext.authservice.dtos.request.LoginRequest;
import org.upnext.authservice.dtos.request.PasswordResetRequest;
import org.upnext.authservice.dtos.request.RegisterRequest;
import org.upnext.authservice.dtos.response.LoginResponse;
import org.upnext.authservice.enums.TokenType;
import org.upnext.authservice.exceptions.EmailAlreadyUsed;
import org.upnext.authservice.exceptions.TokenNotFound;
import org.upnext.authservice.models.Token;
import org.upnext.authservice.models.User;
import org.upnext.authservice.repositories.TokensRepository;
import org.upnext.authservice.services.AuthService;
import org.upnext.authservice.services.UserService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TokensRepository tokensRepository;

    @Value("${frontend.url}")
    String frontUrl;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user;
        return null;
    }

    @Override
    public void register(RegisterRequest registerRequest) {
        // if email exists throws an exception
        checkExistingAccount(registerRequest.getEmail());

        User user = extractUserFromRegister(registerRequest);

        userService.save(user);

        // publish event for the mail service to send confirmation
        sendConfirmationMail(user);
    }

    @Override
    public void confirmAccount(String token) {
        Token tokenObj = tokensRepository.findByTokenAndType(token, TokenType.ACCOUNT_CONFIRM)
                .orElseThrow(() -> new TokenNotFound("Invalid or unknown token!"));

        verifyToken(tokenObj);


        User user = tokenObj.getUser();
        user.setIsConfirmed(true);
        tokenObj.setIsUsed(true);
        userService.save(user);
        tokensRepository.save(tokenObj);

    }

    private void verifyToken(Token token) {
        if (token.getIsUsed()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Token has already been used");
        }

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Token has expired");
        }

    }

    @Override
    public void requireConfirmation(EmailRequest emailRequest) {
        String email = emailRequest.getEmail();
        User user = userService.findByEmail(email);
        sendConfirmationMail(user);
    }

    @Override
    public void sendPasswordResetMail(EmailRequest emailRequest) {
        String email = emailRequest.getEmail();

        User user = userService.findByEmail(email);


        Token token = Token.builder()
                .isUsed(false)
                .user(user)
                .expiryDate(LocalDateTime.now())
                .type(TokenType.PASSWORD_RESET)
                .build();

        token = tokensRepository.save(token);

        String resetUrl = frontUrl + "/reset-password?token=" + token.getToken();
        // TODO publish event to send email
    }

    @Override
    public void passwordReset(PasswordResetRequest passwordResetRequest) {

        Token token = tokensRepository.findByTokenAndType(passwordResetRequest.getToken(), TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new TokenNotFound("Invalid or unknown token!"));

        verifyToken(token);
        token.setIsUsed(true);

        String password = passwordResetRequest.getNewPassword();
        String hashedPassword = passwordEncoder.encode(password);

        userService.updatePassword(token.getUser(), hashedPassword);


    }

    private void checkExistingAccount(String email) {
        if (userService.existsByEmail(email)) {
            throw new EmailAlreadyUsed("Email already used!");
        }
    }

    private User extractUserFromRegister(RegisterRequest registerRequest) {
        User user = new User();
        user.setName(registerRequest.getName());
        user.setAddress(registerRequest.getAddress());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return user;

    }

    private void sendConfirmationMail(User user) {

        Token token = Token.builder()
                .user(user)
                .type(TokenType.ACCOUNT_CONFIRM)
                .isUsed(false)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();

        token = tokensRepository.save(token);

        String confirmationLink = frontUrl + "/auth/confirm?token=" + token.getToken();

        // TODO publish event to send email
    }


}
