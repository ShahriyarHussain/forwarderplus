package com.lazoft.forwarderplus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class TransactionLeg {

    @Id
    private Long legId;
    private Integer slNo;
    private BigDecimal amount;
    private String remarks;
}
