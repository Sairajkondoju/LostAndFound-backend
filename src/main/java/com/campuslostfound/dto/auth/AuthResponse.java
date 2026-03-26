package com.campuslostfound.dto.auth;

import com.campuslostfound.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private Role role;
}
