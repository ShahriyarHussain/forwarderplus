package com.lazoft.forwarderplus.service.impl;

import com.lazoft.forwarderplus.dto.RegisterUserDto;
import com.lazoft.forwarderplus.entity.User;
import com.lazoft.forwarderplus.enums.Role;
import com.lazoft.forwarderplus.service.RegisterService;
import com.lazoft.forwarderplus.services.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.CharUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Value("${dev.env}")
    private boolean isDevEnv;

    @Override
    public void registerUser(RegisterUserDto dto) {
        User user = new User();
        user.setUsername(dto.userName());
        user.setName(dto.fullName());
        user.setEmail(dto.email());
        user.setHashedPassword(passwordEncoder.encode(dto.password()));
        user.setContactNo(dto.countryCode().getCountryCode() + dto.contactNo());
        user.setDesignation(dto.designation());
        user.setRoles(Set.of(Role.USER));
        user.setUserNotLocked(true);
        user.setNotTerminated(true);
        user.setEnabled(false);
        user.setPasswordNotExpired(true);
        user.setCreatedOn(LocalDateTime.now());

        if (isFirstUser()) {
            user.setRoles(Set.of(Role.ADMIN));
            user.setEnabled(true);
        }

        userService.create(user);
    }

    @Override
    public boolean isUserNameExists(String userName) {
        return userService.existsByUsername(userName);
    }

    @Override
    public boolean isEmailExists(String email) {
        return userService.existsByEmail(email);
    }

    @Override
    public boolean isStrongPassword(String password) {
        if (isDevEnv) {
            return true;
        }
        if (password.length() < 6) {
            return false;
        }

        boolean containsUpperCase = false ;
        boolean containsNumeric = false;
        for (char c : password.toCharArray()) {
            if (CharUtils.isAsciiNumeric(c)) {
                containsUpperCase = true;
                continue;
            }
            if (CharUtils.isAsciiAlphaUpper(c)) {
                containsNumeric = true;
            }
        }
        return containsUpperCase && containsNumeric;
    }

    private boolean isFirstUser() {
        return userService.count() == 0;
    }
}
