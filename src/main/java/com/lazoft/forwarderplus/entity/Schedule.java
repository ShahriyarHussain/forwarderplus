package com.lazoft.forwarderplus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule_id_generator")
    @SequenceGenerator(name = "schedule_id_generator")
    private Long scheduleId;

    @OneToOne
    private Port portOfLoading;
    private LocalDate portOfLoadingETA;
    private LocalDate portOfLoadingETD;

    @OneToOne
    private Port portOfDischarge;
    private LocalDate portOfDischargeETA;

    @OneToOne
    private Port motherVesselPort;
    private LocalDate motherVesselETA;

    @OneToMany(mappedBy = "schedule", fetch = FetchType.LAZY)
    private Set<Shipment> shipment;

}
