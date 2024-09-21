package com.lazoft.forwarderplus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class StuffingDetails {
    private String cnfAgentName;
    private String cnfAgentContactNo;

    @OneToOne
    private StuffingDepot stuffingDepot;
    private LocalDate stuffingDate;
    private BigDecimal stuffingCharge;

    @Id
    @OneToOne
    private Shipment shipment;
}
