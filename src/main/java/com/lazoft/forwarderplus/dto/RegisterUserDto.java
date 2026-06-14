package com.lazoft.forwarderplus.dto;

import com.lazoft.forwarderplus.enums.CountryCode;

public record RegisterUserDto(
        String userName,
        String fullName,
        String email,
        String password,
        CountryCode countryCode,
        String contactNo,
        String designation
) {
}
