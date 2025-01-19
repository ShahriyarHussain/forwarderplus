package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.AmountCurrency;
import com.lazoft.forwarderplus.enums.TransactionMethod;
import com.lazoft.forwarderplus.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Transaction extends AbstractEntity {
    private TransactionMethod transactionMethod;
    private LocalDateTime transactionTime;
    private BigDecimal conversionRate;
    private LocalDate transactionDate;
    private AmountCurrency currency;
    private BigDecimal totalAmount;
    private TransactionType type;
    private String description;
    private String remarks;
    @OneToOne
    private User user;


    @OneToOne
    private Account account;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<TransactionLeg> transactionLegs;


}
