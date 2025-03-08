package com.lazoft.forwarderplus.entity;

import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@EqualsAndHashCode(callSuper = false)
public class TransactionLeg extends AbstractEntity {
    private Integer slNo;
    private BigDecimal amount;
    private String remarks;
}
