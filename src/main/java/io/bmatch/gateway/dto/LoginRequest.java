package io.bmatch.gateway.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String password;
    private String email;
}
