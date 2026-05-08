package com.example.ExpenseTracker.config.jwtConfig;
import com.example.ExpenseTracker.service.JwtFilterService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtEntryPoint jwtEntryPoint;
    private final JwtFilterService jwtFilterService;
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final Cookie[] cookies = request.getCookies();
         String jwt = null;
        if (cookies != null) {
            for(Cookie cookie : cookies){
                if("jwtToken".equals(cookie.getName())){
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        if(jwt == null){
            filterChain.doFilter(request, response);
            return;
        }
       try{
           jwtFilterService.validateTokenAndUser(jwt);
       }
       catch(JwtException | UsernameNotFoundException ex){
           logger.warn("JWT authentication failed for URI {}: ", request.getRequestURI());
           jwtEntryPoint.commence(request, response, new BadCredentialsException("Invalid credentials"));
           return;
       }
       catch (Exception ex){
           logger.error("Unexpected error during JWT validation for URI {}", request.getRequestURI());
           jwtEntryPoint.commence(request, response, new InternalAuthenticationServiceException("Internal server error during authentication"));
           return;
       }

        filterChain.doFilter(request, response);
    }
}