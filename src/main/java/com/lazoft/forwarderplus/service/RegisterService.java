package com.lazoft.forwarderplus.service;

import com.lazoft.forwarderplus.dto.RegisterUserDto;

public interface RegisterService {

    void registerUser(RegisterUserDto dto);

    boolean isUserNameExists(String userName);

    boolean isEmailExists(String email);

    boolean isStrongPassword(String password);
}
