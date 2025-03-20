package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.PackageUnit;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

    private String cnfAgentName;
    private String cnfAgentContactNo;
    private String vessel;
    private LocalDate stuffingDate;
    private BigDecimal stuffingCharge;
    private Integer quantity;
    private PackageUnit packageUnit;
    private String stuffingDepot;
}


