package com.lazoft.forwarderplus.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lazoft.forwarderplus.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "application_user")
public class User {

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

}
