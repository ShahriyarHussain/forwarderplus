package com.lazoft.forwarderplus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Transshipment {

    @Id
    private long scheduleId;

    @OneToOne
    private Schedule schedule;

    @ManyToOne
    private Port vesselPort;
    private String vesselName;
    private LocalDate portEta;
    private String remarks;
}
