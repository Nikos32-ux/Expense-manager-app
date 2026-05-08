package com.example.ExpenseTracker.service;
import com.example.ExpenseTracker.config.jwtConfig.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class JwtFilterService {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(JwtFilterService.class);

    public void validateTokenAndUser(String jwt){
        String email = jwtUtils.extractEmail(jwt);

        if(email != null && SecurityContextHolder.getContext().getAuthentication() == null){
           UserDetails userDetails = userDetailsService.loadUserByUsername(email);
           if(jwtUtils.validateToken(email, userDetails)){
               logger.info("Token validated successfully for user: {}", email);
               UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                       userDetails,
                       null,
                       userDetails.getAuthorities()
               );
               SecurityContextHolder.getContext().setAuthentication(token);
           }else{
               logger.info("Token validated failed for user: {}", email);
           }
        }
    }

}
