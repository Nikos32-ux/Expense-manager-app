package com.example.ExpenseTracker.config.jwtConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        ObjectMapper objectMapper = new ObjectMapper();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        //response.getWriter().write("{\"error}");

        Map<String, Object> data = new HashMap<>();
        data.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        data.put("message", authException.getMessage());
        data.put("timestamp", new Date(System.currentTimeMillis()));

        objectMapper.writeValue(response.getOutputStream(), data);
    }
}
