package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.PackageUnit;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class ContainerDetails {

    @Id
    private long containerDetailsId;
    private String containerNo;
    private String sealNo;
    private BigDecimal grossWeight;
    private int noOfPackages;
    private PackageUnit packageUnit;
}
