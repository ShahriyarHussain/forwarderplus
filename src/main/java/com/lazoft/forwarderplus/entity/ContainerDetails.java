package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.PackageUnit;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import org.atmosphere.config.service.Get;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class ContainerDetails {
    private String containerNo;
    private String sealNo;
    private BigDecimal grossWeight;
    private int noOfPackages;
    private PackageUnit packageUnit;

    @Id
    @OneToOne
    private Shipment shipment;
}
