package quest.dine.gateway.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import quest.dine.gateway.enums.PermissionLevel;
import quest.dine.gateway.enums.Status;

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
    private String email;
    private String password;
    private PermissionLevel role;
    private String tag;
    private Status status;

    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;
    private boolean isEnabled;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority(role.name()));

        authorities.add(new SimpleGrantedAuthority(status.name()));

        return authorities;
    }
}
