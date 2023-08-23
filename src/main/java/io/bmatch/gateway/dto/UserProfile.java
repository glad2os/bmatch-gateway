package io.bmatch.gateway.dto;

import io.bmatch.gateway.enums.PermissionLevel;
import io.bmatch.gateway.enums.Status;
import io.bmatch.gateway.model.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserProfile {
    private Long id;
    private String username;
    private String email;
    private Integer mmr;
    private PermissionLevel role;
    private String tag;
    private Status status;
    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;
    private boolean isEnabled;

    public UserProfile(Long id, String username, String email, Integer mmr, PermissionLevel role, String tag, Status status, boolean isAccountNonExpired, boolean isAccountNonLocked, boolean isCredentialsNonExpired, boolean isEnabled) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.mmr = mmr;
        this.role = role;
        this.tag = tag;
        this.status = status;
        this.isAccountNonExpired = isAccountNonExpired;
        this.isAccountNonLocked = isAccountNonLocked;
        this.isCredentialsNonExpired = isCredentialsNonExpired;
        this.isEnabled = isEnabled;
    }

    public static UserProfile ofUser(User user) {
        return new UserProfile(user.getId(), user.getUsername(), user.getEmail(), user.getMmr(), user.getRole(), user.getTag(), user.getStatus(), user.isAccountNonExpired(), user.isAccountNonLocked(), user.isCredentialsNonExpired(), user.isEnabled());
    }
}
