package org.upnext.authservice.controllers;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.upnext.authservice.services.AuthService;

@RequiredArgsConstructor
@WebMvcTest(controllers = AuthController.class)
public class AuthControllerTest {
    @MockitoBean
    private final AuthService authService;
    private final MockMvc mockMvc;

}
