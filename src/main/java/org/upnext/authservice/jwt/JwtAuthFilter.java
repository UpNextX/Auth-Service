package org.upnext.authservice.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.upnext.authservice.models.User;
import org.upnext.authservice.services.UserService;
import java.io.IOException;
import java.util.Enumeration;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {
    Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtils jwtUtils;
    @Lazy
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.info("doFilterInternal Started");
        Enumeration<String> headerNames = request.getHeaderNames();
       while(headerNames.hasMoreElements()) {
            System.out.println(headerNames.nextElement() + " : " + request.getHeader(headerNames.nextElement()) );
        }
        String token = jwtUtils.getJwtFromHeader(request);
        if (token != null && jwtUtils.isValidToken(token)) {
            Long id = Long.parseLong(jwtUtils.extractId(token));
            User user = userService.loadUserObjectById(id);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            //user.getAuthorities().stream().forEach(item -> System.out.println(item.getAuthority()));
            //logger.info("User Roles {}", user.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        filterChain.doFilter(request, response);
    }


}
