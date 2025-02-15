package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.AmountCurrency;
import com.lazoft.forwarderplus.enums.LedgerTransactionType;
import com.lazoft.forwarderplus.enums.TransactionMethod;
import com.lazoft.forwarderplus.enums.TransactionType;
import jakarta.persistence.*;
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
@Table(indexes = @Index(name = "batchDateIdx", columnList = "businessDate, batchNo", unique = true))
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

    @ManyToOne
    private User user;
    @ManyToOne
    private Account fromAccount;
    @ManyToOne
    private Account toAccount;
    @ManyToOne
    private Ledger fromLedger;
    @ManyToOne
    private Ledger toLedger;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<TransactionLeg> transactionLegs;

    @Transient
    private Map<String, TransactionType> ledgerTransactionType;


}
