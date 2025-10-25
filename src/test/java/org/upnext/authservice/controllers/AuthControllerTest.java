package org.upnext.authservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.upnext.authservice.configurations.SecurityConfig;
import org.upnext.authservice.jwt.JwtAuthFilter;
import org.upnext.authservice.jwt.JwtEntryPoint;
import org.upnext.authservice.services.AuthService;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    AuthService authService;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    JwtEntryPoint jwtEntryPoint;
    @Test
    public void testLoginAlreadyAuthenticated() throws Exception {

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect((ResultMatcher) content().string("Already Signed in!"));
    }

}
