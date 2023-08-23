package io.bmatch.gateway.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.bmatch.gateway.helpers.authority.GrantedAuthorityDeserializer;
import io.bmatch.gateway.helpers.email.ValidEmailFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import io.bmatch.gateway.enums.PermissionLevel;
import io.bmatch.gateway.enums.Status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Table("users")
@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class User implements UserDetails {

    @Id
    @Column
    private Long id;

    private String username;

    @ValidEmailFormat
    private String email;
    private Integer mmr;
    private String password;
    private PermissionLevel role;
    private String tag;
    private Status status;

    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;
    private boolean isEnabled;

    @JsonDeserialize(using = GrantedAuthorityDeserializer.class)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (role != null) {
            authorities.add(new SimpleGrantedAuthority(role.getPermission()));
        }

        if (status != null) {
            authorities.add(new SimpleGrantedAuthority(status.name()));
        }

        return authorities;
    }

}
