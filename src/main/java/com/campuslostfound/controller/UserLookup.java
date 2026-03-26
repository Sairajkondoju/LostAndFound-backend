package com.campuslostfound.controller;

import com.campuslostfound.exception.ResourceNotFoundException;
import com.campuslostfound.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserLookup {

    private final UserRepository userRepository;

    public Long getUserId(Principal principal, HttpServletRequest request) {
        String headerUserId = request.getHeader("X-User-Id");
        if (headerUserId != null && !headerUserId.isBlank()) {
            try {
                return Long.parseLong(headerUserId);
            } catch (NumberFormatException ex) {
                throw new ResourceNotFoundException("Invalid user id header");
            }
        }

        if (principal == null) {
            throw new ResourceNotFoundException("Logged-in user not found");
        }
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"))
                .getId();
    }
}
