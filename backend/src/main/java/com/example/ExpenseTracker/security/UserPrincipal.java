package com.example.ExpenseTracker.security;
import com.example.ExpenseTracker.model.Capabilities;
import com.example.ExpenseTracker.model.Roles;
import com.example.ExpenseTracker.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class UserPrincipal implements UserDetails {
    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    @Override public String getUsername() { return user.getEmail(); }
    @Override public String getPassword() { return user.getPassword(); }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        for(Roles role : user.getRoles()){
            authorities.add(new SimpleGrantedAuthority(role.getRoleType().toString()));

            for(Capabilities cap : role.getCapabilities()){
                authorities.add(new SimpleGrantedAuthority(cap.getCapabilityType()));
            }
        }

        return authorities;
    }
    public Long getId(){return user.getId();}
    public String getEmail(){return user.getEmail();}
    public String getImageProfile(){return user.getImageProfile();}
    public String getDisplayName() {return user.getUsername();}
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

}
