package com.lazoft.forwarderplus.entity.finance;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@Entity
public class TransactionLeg extends AccountingBaseEntity {

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
    private BigDecimal debitAmount = BigDecimal.ZERO;
    private BigDecimal creditAmount = BigDecimal.ZERO;
    private String legRemarks;
}
