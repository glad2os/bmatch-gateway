package io.bmatch.gateway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9_.-]+$")
    private String username;
    @NotBlank
    @Size(min = 6, max = 72)
    private String password;
    @NotBlank
    @Email
    private String email;
}
