package io.bmatch.gateway.dto;

import io.bmatch.gateway.helpers.profile.ProfileBackgrounds;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateProfileRequest {
    // Same rules as registration: the username is part of lobby ids.
    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9_.-]+$")
    private String username;
    @Size(max = 50)
    private String firstName;
    @Size(max = 50)
    private String lastName;
    @Past
    private LocalDate birthday;
    @Size(max = 5)
    private List<@NotBlank @Size(max = 200) @Pattern(regexp = "^https?://\\S+$") String> links;
    @Pattern(regexp = ProfileBackgrounds.PATTERN)
    private String background;
}
