package org.upnext.authservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.upnext.authservice.dtos.request.LoginRequest;
import org.upnext.authservice.dtos.request.RegisterRequest;
import org.upnext.authservice.jwt.JwtAuthFilter;
import org.upnext.authservice.services.AuthService;
import org.upnext.authservice.services.UserService;
import org.upnext.sharedlibrary.Dtos.UserDto;
import org.upnext.sharedlibrary.Errors.Error;
import org.upnext.sharedlibrary.Errors.Result;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    public void givenInvalidData_whenRegister_thenBadRequest() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("");
        registerRequest.setPhoneNumber("01512345678");
        registerRequest.setEmail("invalidemail");
        registerRequest.setEmailConfirm("invalidemail");
        registerRequest.setPassword("123");
        registerRequest.setPasswordConfirm("123");
        registerRequest.setAddress("");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    // password does not match
    @Test
    public void givenInvalidData_whenRegister_thenBadRequest2() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Ahmed");
        registerRequest.setPhoneNumber("01011111111");
        registerRequest.setEmail("eslam@gmail.com");
        registerRequest.setEmailConfirm("eslam@gmail.com");
        registerRequest.setPassword("eslam291823@J");
        registerRequest.setPasswordConfirm("123434534aer56");
        registerRequest.setAddress("Cairo");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenValidData_whenRegister_thenCreated() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Ahmed");
        registerRequest.setPhoneNumber("01011111111");
        registerRequest.setEmail("eslam@gmail.com");
        registerRequest.setEmailConfirm("eslam@gmail.com");
        registerRequest.setPassword("eslam291823@J");
        registerRequest.setPasswordConfirm("eslam291823@J");
        registerRequest.setAddress("Cairo");

        doNothing().when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    public void givenInvalidCredentials_whenLogin_thenUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest("eslam@email.com", "wrongPassword@1");

        Result<UserDto> failureResult = Result.failure(new Error("User.Invalid", "No user with this credentials!", 401));
        when(authService.login(any(LoginRequest.class), any(HttpServletResponse.class))).thenReturn(failureResult);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenValidCredentials_whenLogin_thenOk() throws Exception {
        LoginRequest request = new LoginRequest("eslam@email.com", "Correct@2121");
        UserDto user = new UserDto();
        user.setEmail("eslam@email.com");
        user.setId(1L);
        Result<UserDto> successResult = Result.success(user);
        when(authService.login(any(LoginRequest.class), any(HttpServletResponse.class))).thenReturn(successResult);
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
