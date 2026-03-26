package com.campuslostfound.config;

import com.campuslostfound.exception.UnauthorizedException;
import com.campuslostfound.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class RequestAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        if ("POST".equalsIgnoreCase(method)) {
            return !path.equals("/api/lost-items")
                    && !path.equals("/api/found-items")
                    && !path.equals("/api/claims/request")
                    && !path.equals("/api/uploads");
        }

        if ("GET".equalsIgnoreCase(method) && path.matches("^/api/items/\\d+/poster$")) {
            return false;
        }

        if ("GET".equalsIgnoreCase(method) && path.matches("^/api/lost-items/\\d+/poster$")) {
            return false;
        }

        return !path.startsWith("/api/admin");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String headerUserId = request.getHeader("X-User-Id");
        if (headerUserId == null || headerUserId.isBlank()) {
            throw new UnauthorizedException("Login required");
        }

        Long userId;
        try {
            userId = Long.parseLong(headerUserId);
        } catch (NumberFormatException ex) {
            throw new UnauthorizedException("Invalid user id header");
        }

        if (!userRepository.existsById(userId)) {
            throw new UnauthorizedException("Logged-in user not found");
        }

        filterChain.doFilter(request, response);
    }
}
