package org.upnext.authservice.services.Impl;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokensRepository tokensRepository;
    private final JwtUtils jwtUtils;

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
    public void register(RegisterRequest registerRequest) {
        // if email exists throws an exception
        checkExistingAccount(registerRequest.getEmail());

        User user = extractUserFromRegister(registerRequest);

        user = userService.save(user);

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

    @Override
    public void logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setMaxAge(0);
        jwtCookie.setSecure(false);
        jwtCookie.setHttpOnly(true);
        response.addCookie(jwtCookie);
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
