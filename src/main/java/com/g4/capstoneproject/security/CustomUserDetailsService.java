package com.g4.capstoneproject.security;

import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom UserDetailsService for Spring Security
 * Loads user from database and wraps in CustomUserDetails
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        // Try to find user by email or phone number
        User user = userRepository.findByEmailOrPhoneNumber(username, username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        // Check if user is active
        if (!user.getIsActive()) {
            log.warn("User account is disabled: {}", username);
            throw new UsernameNotFoundException("User account is disabled");
        }

        log.debug("User loaded successfully: {} with role {}", username, user.getRole());

        // Wrap User entity in CustomUserDetails
        return new CustomUserDetails(user);
    }
}
