package com.lazoft.forwarderplus.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lazoft.forwarderplus.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
@Entity
@Table(name = "application_user")
public class User implements UserDetails {

    @Id
    private String username;
    private String name;

    @JsonIgnore
    private String hashedPassword;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;

    private String contactNo;
    private String email;
    private String designation;
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;

    private boolean isUserNotLocked;
    private boolean isPasswordNotExpired;
    private boolean isEnabled;
    private boolean isNotTerminated;

    private int invalidAttempts;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return hashedPassword;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isNotTerminated;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isUserNotLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isPasswordNotExpired;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
