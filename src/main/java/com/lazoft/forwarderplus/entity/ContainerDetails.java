package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.PackageUnit;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ContainerDetails {

    @Id
    private String containerDetailsId;
    private String containerNo;
    private String sealNo;
    private BigDecimal grossWeight;
    private int noOfPackages;
    private PackageUnit packageUnit;

    public ContainerDetails(String containerNo, String sealNo, BigDecimal grossWeight, int noOfPackages,
                            PackageUnit packageUnit, long shipmentId) {
        this.containerDetailsId = shipmentId + containerNo + sealNo;
        this.containerNo = containerNo;
        this.sealNo = sealNo;
        this.grossWeight = grossWeight;
        this.noOfPackages = noOfPackages;
        this.packageUnit = packageUnit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContainerDetails that = (ContainerDetails) o;
        return Objects.equals(containerDetailsId, that.containerDetailsId);
    }
}
