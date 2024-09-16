package com.lazoft.forwarderplus.entity;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Entity
public class SamplePerson extends AbstractEntity {

    private String firstName;
    private String lastName;

    @Email
    private String email;

    private String phone;
    private LocalDate dateOfBirth;
    private String occupation;
    private String role;
    private boolean important;

}
