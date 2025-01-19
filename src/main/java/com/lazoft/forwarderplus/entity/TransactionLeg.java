package com.lazoft.forwarderplus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.math.BigDecimal;

@Entity
public class TransactionLeg {

    @Id
    private Long id;
    private Integer slNo;
    private BigDecimal amount;
    private String remarks;

}
