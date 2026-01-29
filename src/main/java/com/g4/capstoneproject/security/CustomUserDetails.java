package com.g4.capstoneproject.security;

import com.g4.capstoneproject.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetails implementation for Spring Security
 * Wraps the User entity to provide authentication information
 */
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return role with ROLE_ prefix as required by Spring Security
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        // Use email as username, fallback to phone if email is null
        return user.getEmail() != null ? user.getEmail() : user.getPhoneNumber();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getIsActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getIsActive();
    }

    /**
     * Get the underlying User entity
     */
    public User getUser() {
        return user;
    }

    /**
     * Get user ID
     */
    public Long getId() {
        return user.getId();
    }

    /**
     * Get full name
     */
    public String getFullName() {
        return user.getFullName();
    }

    /**
     * Get role
     */
    public User.UserRole getRole() {
        return user.getRole();
    }
}
