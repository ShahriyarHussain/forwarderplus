package com.lazoft.forwarderplus.security;

import com.lazoft.forwarderplus.entity.User;
import com.lazoft.forwarderplus.repository.UserRepository;
import com.lazoft.forwarderplus.services.UserService;
import com.vaadin.flow.spring.security.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthenticatedUser {

    private final UserService userService;
    private final AuthenticationContext authenticationContext;

    @Transactional
    public Optional<User> get() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .map(userDetails -> userService.get(userDetails.getUsername()).orElseThrow(() ->
                        new UsernameNotFoundException("User not found")));
    }

    public void logout() {
        authenticationContext.logout();
    }

}
