package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.AccountType;
import com.lazoft.forwarderplus.enums.AmountCurrency;
import com.lazoft.forwarderplus.enums.TransactionType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.math.BigDecimal;

@Entity
public class Account {

    @Id
    private Long id;
    private String name;
    private String accountNo;
    private AccountType accountType;
    private BigDecimal startingBalance;
    private BigDecimal currentBalance;
    private AmountCurrency amountCurrency;
    private String financialDetails;
}
