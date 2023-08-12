package io.bmatch.gateway.dto;

import io.bmatch.gateway.enums.PermissionLevel;
import io.bmatch.gateway.model.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class UserProfile {
    private String username;
    private String email;
    private Integer mmr;
    private PermissionLevel role;

    public UserProfile(String username, String email, Integer mmr, PermissionLevel role) {
        this.username = username;
        this.email = email;
        this.mmr = mmr;
        this.role = role;
    }

    public static UserProfile ofUser(User user) {
        return new UserProfile(user.getUsername(),user.getEmail(), user.getMmr(), user.getRole());
    }
}
