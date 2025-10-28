package org.upnext.authservice.services.Impl;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.upnext.authservice.dtos.response.LoginResponse;
import org.upnext.authservice.jwt.JwtUtils;
import org.upnext.authservice.dtos.request.EmailRequest;
import org.upnext.authservice.dtos.request.LoginRequest;
import org.upnext.authservice.dtos.request.PasswordResetRequest;
import org.upnext.authservice.dtos.request.RegisterRequest;
import org.upnext.authservice.enums.TokenType;
import org.upnext.authservice.exceptions.EmailAlreadyUsed;
import org.upnext.authservice.exceptions.TokenNotFound;
import org.upnext.authservice.mappers.UserMapper;
import org.upnext.authservice.models.Token;
import org.upnext.authservice.models.User;
import org.upnext.authservice.repositories.TokensRepository;
import org.upnext.authservice.services.AuthService;
import org.upnext.authservice.services.UserService;
import org.upnext.sharedlibrary.Dtos.UserDto;
import org.upnext.sharedlibrary.Errors.Error;
import org.upnext.sharedlibrary.Errors.Result;
import org.upnext.sharedlibrary.Events.MailEvent;

import java.time.LocalDateTime;

import static org.upnext.authservice.configurations.RabbitMqAccountConfirmConfig.EMAIL_CONFIRM_EXCHANGE;
import static org.upnext.authservice.configurations.RabbitMqAccountConfirmConfig.EMAIL_CONFIRM_ROUTING_KEY;
import static org.upnext.authservice.configurations.RabbitMqPasswordResetConfig.FORGET_PASS_EXCHANGE;
import static org.upnext.authservice.configurations.RabbitMqPasswordResetConfig.FORGET_PASS_ROUTING_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokensRepository tokensRepository;
    private final JwtUtils jwtUtils;
    private final RabbitTemplate rabbitTemplate;

    @Value("${frontend.url}")
    String frontUrl;

    @Override
    public Result<UserDto> login(LoginRequest loginRequest, HttpServletResponse response) {
        // if the user does not exists it will throw not found exception
        User user = userService.loadUserByEmail(loginRequest.getEmail());

        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return Result.failure(new Error("User.Invalid", "No user with this credentials!", 401));
        }

        String token = jwtUtils.generateToken(user);

        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setMaxAge(24 * 60 * 60);
        response.addCookie(jwtCookie);

        return Result.success(userMapper.toUserDto(user)) ;
    }

    @Override
    @Transactional
    public Result<UserDto> register(RegisterRequest registerRequest, HttpServletResponse response) {
        // if email exists throws an exception
        if(checkExistingAccount(registerRequest.getEmail())){
            throw new EmailAlreadyUsed("Email already used!");
        }

        User user = userMapper.toUserFromRegisterRequest(registerRequest);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user = userService.save(user);
       // System.out.println(user);

        // publish event for the mail service to send confirmation
        sendConfirmationMail(user);

        return login(new LoginRequest(registerRequest.getEmail(), registerRequest.getPassword()), response);
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
    public Result<String> requireConfirmation(EmailRequest emailRequest) {
        String email = emailRequest.getEmail();
        User user = userService.loadUserByEmail(email);
        if(user.getIsConfirmed()){
            return Result.failure(new Error("Account.AlreadyConfirmed", "Account already confirmed!", 400));
        }
        sendConfirmationMail(user);

        return Result.success("Confirmation mail send.");
    }

    @Override
    public void sendPasswordResetMail(EmailRequest emailRequest) {
        String email = emailRequest.getEmail();

        User user = userService.loadUserByEmail(email);


        sendResetPasswordMail(user);
    }

    @Override
    public void passwordReset(String tokenStr, String password) {
        if(tokenStr == null) {
            throw new TokenNotFound("Invalid or unknown token!");
        }
        Token token = tokensRepository.findByTokenAndType(tokenStr, TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new TokenNotFound("Invalid or unknown token!"));

        verifyToken(token);
        token.setIsUsed(true);

        String hashedPassword = passwordEncoder.encode(password);

        userService.updatePassword(token.getUser(), hashedPassword);


    }

    @Override
    public void logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setMaxAge(0);
        jwtCookie.setSecure(false);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        response.addCookie(jwtCookie);
    }

    private Boolean checkExistingAccount(String email) {
        return userService.existsByEmail(email);
    }


    private void sendConfirmationMail(User user) {

        Token token = Token.builder()
                .user(user)
                .type(TokenType.ACCOUNT_CONFIRM)
                .isUsed(false)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();

        token = tokensRepository.save(token);

        String confirmationLink = frontUrl + "/auth/confirm.html?token=" + token.getToken();

        MailEvent mailEvent = new MailEvent();
        mailEvent.setEmail(user.getEmail());
        mailEvent.setName(user.getName());
        mailEvent.setConfirmation_url(confirmationLink);
        System.out.println("Email Confirmation Link Will Be Sent");
        rabbitTemplate.convertAndSend(EMAIL_CONFIRM_EXCHANGE, EMAIL_CONFIRM_ROUTING_KEY, mailEvent);
    }

    private void sendResetPasswordMail(User user){
        Token token = Token.builder()
                .isUsed(false)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(4))
                .type(TokenType.PASSWORD_RESET)
                .build();

        token = tokensRepository.save(token);

        String resetUrl = frontUrl + "/auth/reset-password.html?token=" + token.getToken();
        MailEvent mailEvent = new MailEvent();
        mailEvent.setEmail(user.getEmail());
        mailEvent.setName(user.getName());
        mailEvent.setConfirmation_url(resetUrl);
        System.out.println("Email Reset Link Will Be Sent");
        rabbitTemplate.convertAndSend(FORGET_PASS_EXCHANGE, FORGET_PASS_ROUTING_KEY, mailEvent);
    }


}
