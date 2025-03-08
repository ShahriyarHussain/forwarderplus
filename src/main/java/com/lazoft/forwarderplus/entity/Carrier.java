package com.lazoft.forwarderplus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CARRIER")
@Getter
@Setter
public class Carrier extends AbstractEntity {
    private String name;
    private String country;
}
