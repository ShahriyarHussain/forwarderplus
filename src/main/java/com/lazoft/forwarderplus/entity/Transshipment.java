package com.lazoft.forwarderplus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Transshipment {

    @Transient
    private int sl;

    @Id
    private long transshipmentId;

    @OneToOne
    private Schedule schedule;

    @ManyToOne
    private Port vesselPort;
    private String vesselName;
    private LocalDate portEta;
    private String remarks;
}
