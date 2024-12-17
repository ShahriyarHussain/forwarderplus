package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.PackageUnit;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
                            PackageUnit packageUnit) {
        this.containerDetailsId = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + containerNo + sealNo;
        this.containerNo = containerNo;
        this.sealNo = sealNo;
        this.grossWeight = grossWeight;
        this.noOfPackages = noOfPackages;
        this.packageUnit = packageUnit;
    }


}
