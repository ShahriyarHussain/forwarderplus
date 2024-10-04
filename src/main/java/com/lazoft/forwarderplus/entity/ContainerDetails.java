package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.PackageUnit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.atmosphere.config.service.Get;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class ContainerDetails {

    @Id
    private long containerDetailsId;
    @MapsId
    @OneToOne
    @JoinColumn(name = "shipmentId")
    private Shipment shipment;

    private String containerNo;
    private String sealNo;
    private BigDecimal grossWeight;
    private int noOfPackages;
    private PackageUnit packageUnit;
}
