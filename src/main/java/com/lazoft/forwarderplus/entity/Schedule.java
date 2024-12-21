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

    @ManyToOne
    private Port portOfLoading;
    private LocalDate portOfLoadingETA;
    private LocalDate portOfLoadingETD;

    @ManyToOne
    private Port portOfDestination;
    private LocalDate portOfDestinationETA;

    @ManyToOne
    private Port motherVesselPort;
    private LocalDate motherVesselETA;

    private String feederVesselName;
    private String motherVesselName;

    @OneToMany(mappedBy = "schedule", fetch = FetchType.LAZY)
    private Set<Shipment> shipment;

    @OneToMany(mappedBy = "schedule", fetch = FetchType.EAGER)
    private Set<Transshipment> transshipments;

}
