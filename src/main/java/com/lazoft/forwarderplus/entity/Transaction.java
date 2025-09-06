package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.AmountCurrency;
import com.lazoft.forwarderplus.enums.TransactionMethod;
import com.lazoft.forwarderplus.enums.TransactionType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Table(indexes = @Index(name = "batchDateSlIdx", columnList = "businessDate, batchNo, slNo", unique = true))
public class Transaction extends AbstractEntity {

    private TransactionMethod transactionMethod;
    private LocalDateTime transactionTime;
    private BigDecimal conversionRate;
    private AmountCurrency currency;
    private LocalDate businessDate;
    private LocalDate transactionDate;
    private BigDecimal totalAmount;
    private BigDecimal totalAmountBaseCurrency;
    private TransactionType type;
    private long attachmentId;
    private String remarks;
    private int batchNo;
    private int slNo;

    @ManyToOne
    private User user;
    @ManyToOne
    private Account transactionAccount;
    @ManyToOne
    private Ledger transactionLedger;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<TransactionLeg> transactionLegs;

    @Transient
    private Map<String, TransactionType> ledgerTransactionType;
}
