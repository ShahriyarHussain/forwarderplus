package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.PackageUnit;
import com.vaadin.flow.component.textfield.IntegerField;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class StuffingDetails {

    @Id
    private Long stuffingId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "shipmentId")
    private Shipment shipment;


    private String cnfAgentName;
    private String cnfAgentContactNo;

    @ManyToOne
    private StuffingDepot stuffingDepot;
    private LocalDate stuffingDate;
    private BigDecimal stuffingCharge;
    private Integer quantity;
    private PackageUnit packageUnit;
}


