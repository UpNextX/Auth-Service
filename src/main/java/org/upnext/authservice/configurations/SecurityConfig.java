package org.upnext.authservice.configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.upnext.authservice.jwt.JwtAuthFilter;
import org.upnext.authservice.jwt.JwtEntryPoint;

@Configuration
@EnableWebSecurity(debug = true)
@EnableMethodSecurity

@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final JwtEntryPoint jwtEntryPoint;
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("Filter Chain");
        http.
                csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/auth/**").permitAll()
                                .anyRequest()
                                .authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session ->   session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtEntryPoint));
        return http.build();

    }

}
