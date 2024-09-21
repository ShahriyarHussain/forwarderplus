package com.lazoft.forwarderplus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import java.time.LocalDate;
import java.util.Set;

@Entity
public class Schedule extends AbstractEntity {

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

    //private Set<>
    @OneToMany
    private Set<Shipment> shipment;

}
